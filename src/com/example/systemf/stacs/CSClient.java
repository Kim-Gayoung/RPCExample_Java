package com.example.systemf.stacs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Function;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.example.systemf.sta.ast.App;
import com.example.systemf.sta.ast.Bool;
import com.example.systemf.sta.ast.Clo;
import com.example.systemf.sta.ast.If;
import com.example.systemf.sta.ast.Let;
import com.example.systemf.sta.ast.Num;
import com.example.systemf.sta.ast.PrimTerm;
import com.example.systemf.sta.ast.Req;
import com.example.systemf.sta.ast.Ret;
import com.example.systemf.sta.ast.Str;
import com.example.systemf.sta.ast.Tapp;
import com.example.systemf.sta.ast.Term;
import com.example.systemf.sta.ast.Unit;
import com.example.systemf.sta.ast.Value;
import com.example.systemf.sta.ast.Var;

public class CSClient {
	private static final String OPEN_SESSION = "OPEN_SESSION";
	private static final String CLOSE_SESSION = "CLOSE_SESSION";

	private static final String REQ = "REQ";
	private static final String RET = "RET";
	private static final String REPLY = "REPLY";
	private static final String CALL = "CALL";

	private static final int PORT = 8080;

	private static String[] libraryNames = { "primIsNothing_client", "primFromJust_client", "primOpenFile_client",
			"primCloseFile_client", "primWriteFile_client", "primReadFile_client", "primReadConsole",
			"primWriteConsole", "primToString_client", "primToInt_client", "primToBool_client", "primReverse_client",
			"primAppend_client", "primLength_client", "primGetHour_client", "primGetYear_client", "primGetMonth_client",
			"primGetDay_client", "primGetDate_client" };
	private static ArrayList<String> libs = new ArrayList<>();

	private static HashMap<String, File> fileMap = new HashMap<>();
	
	private FunStore clientFS;
	private String programName;
	private String serverAddr;

	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;

	private JSONParser jsonParser;

	private Integer sessionNum;

	public CSClient(String serverAddr, String programName, FunStore clientFS) {
		this.programName = programName;
		this.clientFS = clientFS;
		sessionNum = null;

		jsonParser = new JSONParser();
		Collections.addAll(libs, libraryNames);

		this.serverAddr = serverAddr;
	}

	private void connectServer() {
		if (socket == null || socket.isClosed()) {
			try {
				socket = new Socket(serverAddr, PORT);

				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeHeader() {
		connectServer();
		try {
			writer.write("GET " + "/rpc/" + programName + " HTTP/1.1.\r\n");
			writer.write("Host: " + socket.getInetAddress().getHostAddress() + "\r\n");
			writer.write("\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Value evalClient(Term m) {

		Function<Let, Term> receiver = mLet -> {
			Term retM;
			try {
				// HTTP_VERSION STATUS_CODE PHRASE
				String statusLine = reader.readLine();

				// 헤더에서 프로그램이 정상적으로 완료되었는지 확인하기 위해 필요함
				int idxVersion = statusLine.indexOf(" ");
				int idxPhrase = statusLine.indexOf(" ", idxVersion + 1);

				int statusCode = Integer.parseInt(statusLine.substring(idxVersion + 1, idxPhrase));

				if (statusCode == 200) {
					String line;
					// entity body 이전인 \r\n에 도달할 때까지 읽기
					while (!(line = reader.readLine()).equals(""))
						;

					String sessionState = reader.readLine(); // sessionState -> CLOSE_SESSION, sessionNum
					String protocol = reader.readLine(); // protocol

					try {
						if (sessionState.equals(CLOSE_SESSION)) {
							sessionNum = null;
						} else {
							sessionNum = Integer.parseInt(sessionState);
						}

						if (protocol.equals(REPLY)) {
							String strReply = reader.readLine();
							JSONObject replyJson = (JSONObject) jsonParser.parse(strReply);
							Value replyVal = JSonUtil.fromJson(replyJson);

							retM = new Let(mLet.getId(), replyVal, mLet.getT2());
						} else if (protocol.equals(CALL)) {
							String strClo = reader.readLine();
							JSONObject cloJson = (JSONObject) jsonParser.parse(strClo);
							Value clo = JSonUtil.fromJson(cloJson);

							int n = Integer.parseInt(reader.readLine());

							ArrayList<Value> args = new ArrayList<>();
							for (int i = 0; i < n; i++) {
								String strArg = reader.readLine();
								JSONObject argJson = (JSONObject) jsonParser.parse(strArg);
								Value arg = JSonUtil.fromJson(argJson);

								args.add(arg);
							}

							retM = new Let(mLet.getId(), new App(clo, args), mLet.getT2());
						} else {
							System.err.println("receiver: Unexpected protocol(" + protocol + ")");
							retM = null;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
						retM = null;
					} catch (ParseException e) {
						e.printStackTrace();
						retM = null;
					}
				} else {
					System.err.println(statusCode);
					retM = null;
				}

			} catch (IOException e) {
				e.printStackTrace();
				retM = null;
			}

			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return retM;
		};

		while (true) {
			System.out.println("CLIENT: " + m);

			if (m instanceof Let) {
				Let mLet = (Let) m;
				Term m1 = mLet.getT1();

				if (m1 instanceof App) {
					App mApp1 = (App) m1;

					Term letBody;
					if (mApp1.getFun() instanceof Clo) {
						Clo fClo = (Clo) mApp1.getFun();
						String f = fClo.getF();

						ClosedFun closedFun = lookup(clientFS, f);

						ArrayList<String> zs = closedFun.getZs();

						letBody = SubstStaCS.substs(
								SubstStaCS.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()), closedFun.getXs(),
								mApp1.getWs());

						m = new Let(mLet.getId(), letBody, mLet.getT2());
					} else if (mApp1.getFun() instanceof Var) {
						Var fVar = (Var) mApp1.getFun();

						letBody = evalLibrary(fVar.getVar(), mApp1.getWs());

						m = new Let(mLet.getId(), letBody, mLet.getT2());
					}
				} else if (m1 instanceof Req) {
					Req mReq1 = (Req) m1;

					if (mReq1.getFun() instanceof Clo) {
						Clo fClo = (Clo) mReq1.getFun();
						ArrayList<Value> ws = mReq1.getWs();
						try {
							writeHeader();
							if (sessionNum != null)
								writer.write(sessionNum + "\n");
							else
								writer.write(OPEN_SESSION + "\n");
							writer.write(REQ + "\n");
							writer.write(fClo.toJson() + "\n");
							writer.write(ws.size() + "\n");
							for (Value w : ws) {
								writer.write(w.toJson() + "\n");
							}
							writer.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}

						m = receiver.apply(mLet);
					}
				} else if (m1 instanceof Tapp) {
					Tapp mTapp1 = (Tapp) m1;

					if (mTapp1.getFun() instanceof Clo) {
						Clo fClo = (Clo) mTapp1.getFun();

						ClosedFun closedFun = lookup(clientFS, fClo.getF());

						m = new Let(mLet.getId(), SubstStaCS.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
								mLet.getT2());
					}
				} else if (m1 instanceof Clo) {
					Clo mClo1 = (Clo) m1;

					m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mClo1);
				} else if (m1 instanceof Unit) {
					Unit mUnit1 = (Unit) m1;

					m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mUnit1);
				} else if (m1 instanceof Num) {
					Num mNum1 = (Num) m1;

					m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mNum1);
				} else if (m1 instanceof Bool) {
					Bool mBool1 = (Bool) m1;

					m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mBool1);
				} else if (m1 instanceof Str) {
					Str mStr1 = (Str) m1;

					m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mStr1);
				} else if (m1 instanceof Let) {
					Let mLet1 = (Let) m1;

					Let let = new Let(mLet1.getId(), mLet1.getT1(), new Let(mLet.getId(), mLet1.getT2(), mLet.getT2()));

					m = let;
				} else if (m1 instanceof Ret) {
					Ret mRet1 = (Ret) m1;
					Value retVal = mRet1.getW();

					try {
						writeHeader();
						writer.write(sessionNum + "\n"); // RET의 경우 sessionNu이 null인 상태는 있을 수가 없음
						writer.write(RET + "\n");
						writer.write(retVal.toJson() + "\n");

						writer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}

					m = receiver.apply(mLet);
				} else if (m1 instanceof If) {
					If mIf1 = (If) m1;

					Term cond = mIf1.getCond();

					if (cond instanceof Bool) {
						Bool boolCond = (Bool) cond;

						if (boolCond.getBool())
							m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), (Value) mIf1.getThenT());
						else
							m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), (Value) mIf1.getElseT());
					}
				} else if (m1 instanceof PrimTerm) {
					PrimTerm mExpr1 = (PrimTerm) m1;
					Term oprnd1;
					Term oprnd2;
					Value v = null;

					switch (mExpr1.getOp()) {
					case 0: // ADD
					case 1: // SUB
					case 2: // MUL
					case 3: // DIV
						oprnd1 = mExpr1.getOprnds().get(0);
						oprnd2 = mExpr1.getOprnds().get(1);

						if (oprnd1 instanceof Num && oprnd2 instanceof Num) {
							Num numOprnd1 = (Num) oprnd1;
							Num numOprnd2 = (Num) oprnd2;

							v = compPrimTerm(numOprnd1, mExpr1.getOp(), numOprnd2);
						} else
							throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1
									+ ") and oprnd2(" + oprnd2 + ") are not Num.");
						break;
					case 4: // UNARY MINUS
						oprnd1 = mExpr1.getOprnds().get(0);

						if (oprnd1 instanceof Num) {
							Num numOprnd1 = (Num) oprnd1;

							v = new Num(-numOprnd1.getI());
						} else
							throw new RuntimeException(
									"StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") is not Num.");
						break;
					case 5: // GTHAN
					case 6: // GEQUAL
					case 7: // LTHAN
					case 8: // LEQUAL
						oprnd1 = mExpr1.getOprnds().get(0);
						oprnd2 = mExpr1.getOprnds().get(1);

						if (oprnd1 instanceof Num && oprnd2 instanceof Num) {
							Num numOprnd1 = (Num) oprnd1;
							Num numOprnd2 = (Num) oprnd2;

							v = compPrimTerm(numOprnd1, mExpr1.getOp(), numOprnd2);
						} else
							throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1
									+ ") and oprnd2(" + oprnd2 + ") are not Num.");
						break;
					case 9: // EQUAL
						oprnd1 = mExpr1.getOprnds().get(0);
						oprnd2 = mExpr1.getOprnds().get(1);

						if (oprnd1.getClass() == oprnd2.getClass()) {
							v = new Bool(Boolean.toString(oprnd1.equals(oprnd2)));
						} else
							throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1
									+ ") and oprnd2(" + oprnd2 + ") are not Equal.");
						break;
					case 10: // NOTEQUAL
						oprnd1 = mExpr1.getOprnds().get(0);
						oprnd2 = mExpr1.getOprnds().get(1);

						if (oprnd1.getClass() == oprnd2.getClass()) {
							v = new Bool(Boolean.toString(!oprnd1.equals(oprnd2)));
						} else
							throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1
									+ ") and oprnd2(" + oprnd2 + ") are not Equal.");
						break;
					case 11:
					case 12:
						oprnd1 = mExpr1.getOprnds().get(0);
						oprnd2 = mExpr1.getOprnds().get(1);

						if (oprnd1 instanceof Bool && oprnd2 instanceof Bool) {
							Bool boolOprnd1 = (Bool) oprnd1;
							Bool boolOprnd2 = (Bool) oprnd2;

							v = compPrimTerm(boolOprnd1, mExpr1.getOp(), boolOprnd2);
						} else
							throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1
									+ ") and oprnd2(" + oprnd2 + ") are not Bool.");
						break;
					case 13:
						oprnd1 = mExpr1.getOprnds().get(0);

						if (oprnd1 instanceof Bool) {
							Bool boolOprnd1 = (Bool) oprnd1;

							v = new Bool(Boolean.toString(!boolOprnd1.getBool()));
						} else
							throw new RuntimeException(
									"StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") is not Bool.");
						break;
					}

					m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), v);
				}
			} else if (m instanceof Clo || m instanceof Unit || m instanceof Num || m instanceof Str
					|| m instanceof Bool)

			{
				return (Value) m;
			} else {
				throw new RuntimeException("StaCsInHttp.evalClient: Must not reach here");
			}
		}

	}

	public static Term evalLibrary(String funName, ArrayList<Value> args) {
		if (funName.equals("primIsNothing_client")) {
			String content = ((Str) args.get(0)).getStr();
			
			if (content == null || content.equals(""))
				return new Bool("False");
			else
				return new Bool("True");
		} else if (funName.equals("primFromJust_client")) {
			String content = ((Str) args.get(0)).getStr();
			
			return new Str(content);
		} else if (funName.equals("primOpenFile_client")) {
			String fileName = ((Str) args.get(0)).getStr();
			String mode = ((Str) args.get(1)).getStr();
			
			
		} else if (funName.equals("primCloseFile_client")) {
			String fileName = ((Str) args.get(0)).getStr();
			
			File file = fileMap.get(fileName);
			
			if (file != null)
				fileMap.remove(fileName);
			
			return new Unit();
		} else if (funName.equals("primWriteFile_client")) {
			String fileName = ((Str) args.get(0)).getStr();
			String content = ((Str) args.get(1)).getStr();
			
			try {
				File file = fileMap.get(fileName);
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
				writer.write(content);
				writer.flush();
				writer.close();
			} catch(IOException e) {
				System.err.println("file is not open.");
			}
			
			return new Unit();
		} else if (funName.equals("primReadFile_client")) {
			String fileName = ((Str) args.get(0)).getStr();
			String ret = "";

			try {
				File file = fileMap.get(fileName);
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					ret += line;
				}

				return new Str(ret);
			} catch (IOException e) {
				System.err.println("file is not exist.");
			}
		} else if (funName.equals("primReadConsole")) {
			Scanner scan = new Scanner(System.in);
			String input = scan.next();

			return new Str(input);
		} else if (funName.equals("primWriteConsole")) {
			String msg = ((Str) args.get(0)).getStr();
			System.out.println(msg);

			return new Unit();
		} else if (funName.equals("primToString_client")) {
			String msg = ((Str) args.get(0)).getStr();

			return new Str(msg);
		} else if (funName.equals("primToInt_client")) {
			String msg = ((Str) args.get(0)).getStr();

			try {
				return new Num(Integer.parseInt(msg));
			} catch (NumberFormatException e) {
				throw new RuntimeException(msg + " is not Number format.");
			}

		} else if (funName.equals("primToBool_client")) {
			String msg = ((Str) args.get(0)).getStr();

			if (msg.equalsIgnoreCase("true"))
				return new Bool("True");
			else
				return new Bool("False");
		} else if (funName.equals("primReverse_client")) {
			String msg = ((Str) args.get(0)).getStr();
			String ret = new StringBuilder(msg).reverse().toString();

			return new Str(ret);
		} else if (funName.equals("primAppend_client")) {
			String msg1 = ((Str)args.get(0)).getStr();
			String msg2 = ((Str)args.get(1)).getStr();

			return new Str(msg1 + msg2);
		} else if (funName.equals("primLength_client")) {
			String msg = ((Str) args.get(0)).getStr();

			return new Num(msg.length());
		} else if (funName.equals("primGetHour_client")) {
			Calendar calendar = Calendar.getInstance();

			return new Num(calendar.get(Calendar.HOUR_OF_DAY));
		} else if (funName.equals("primGetYear_client")) {
			Calendar calendar = Calendar.getInstance();

			return new Num(calendar.get(Calendar.YEAR));

		} else if (funName.equals("primGetMonth_client")) {
			Calendar calendar = Calendar.getInstance();

			return new Num(calendar.get(Calendar.MONTH));

		} else if (funName.equals("primGetDay_client")) {
			Calendar calendar = Calendar.getInstance();

			return new Num(calendar.get(Calendar.DAY_OF_WEEK));

		} else if (funName.equals("primGetDate_client")) {
			Calendar calendar = Calendar.getInstance();

			return new Num(calendar.get(Calendar.DAY_OF_MONTH));

		} else
			throw new RuntimeException(funName + " not supported Library.");

		return null;
	}

	public static Value compPrimTerm(Num oprnd1, int op, Num oprnd2) {
		if (op == 0)
			return new Num(oprnd1.getI() + oprnd2.getI());
		else if (op == 1)
			return new Num(oprnd1.getI() - oprnd2.getI());
		else if (op == 2)
			return new Num(oprnd1.getI() * oprnd2.getI());
		else if (op == 3)
			return new Num(oprnd1.getI() / oprnd2.getI());
		else if (op == 5)
			return new Bool(Boolean.toString(oprnd1.getI() > oprnd2.getI()));
		else if (op == 6)
			return new Bool(Boolean.toString(oprnd1.getI() >= oprnd2.getI()));
		else if (op == 7)
			return new Bool(Boolean.toString(oprnd1.getI() < oprnd2.getI()));
		else if (op == 8)
			return new Bool(Boolean.toString(oprnd1.getI() <= oprnd2.getI()));
		else
			throw new RuntimeException("Not expected Operator " + PrimTerm.get(op));
	}

	public static Value compPrimTerm(Bool oprnd1, int op, Bool oprnd2) {
		if (op == 11)
			return new Bool(Boolean.toString(oprnd1.getBool() && oprnd2.getBool()));
		else if (op == 12)
			return new Bool(Boolean.toString(oprnd1.getBool() || oprnd2.getBool()));
		else
			throw new RuntimeException("Not expected Operator " + PrimTerm.get(op));
	}

	public static ClosedFun lookup(FunStore fs, String f) {
		for (String p : fs.getFs().keySet()) {
			if (p.equals(f))
				return fs.getFs().get(p);
		}
		System.err.println("lookup: Not found: " + f + " in \n" + fs);
		return null;
	}
}
