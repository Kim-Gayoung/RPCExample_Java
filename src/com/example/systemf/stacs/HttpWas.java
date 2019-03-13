package com.example.systemf.stacs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.example.systemf.sta.ast.App;
import com.example.systemf.sta.ast.Bool;
import com.example.systemf.sta.ast.Call;
import com.example.systemf.sta.ast.Clo;
import com.example.systemf.sta.ast.If;
import com.example.systemf.sta.ast.Let;
import com.example.systemf.sta.ast.Num;
import com.example.systemf.sta.ast.PrimTerm;
import com.example.systemf.sta.ast.Str;
import com.example.systemf.sta.ast.Tapp;
import com.example.systemf.sta.ast.Term;
import com.example.systemf.sta.ast.Unit;
import com.example.systemf.sta.ast.Value;
import com.example.systemf.sta.ast.Var;

import javafx.util.Pair;

public class HttpWas {
	private static final String OPEN_SESSION = "OPEN_SESSION";
	private static final String CLOSE_SESSION = "CLOSE_SESSION";

	private static final String REQ = "REQ";
	private static final String RET = "RET";
	private static final String REPLY = "REPLY";
	private static final String CALL = "CALL";

	private static final int PORT = 8080;

	private static HashMap<String, FunStore> programFSMap = new HashMap<>();
	private static HashMap<Integer, CSServer> sessionMap = new HashMap<>();
	private static int count = 0;
	
	private static HashMap<Integer, Pair<String, String>> fileMap = new HashMap<>();
	private static HashMap<Integer, BufferedReader> fileReaders = new HashMap<>();
	private static HashMap<Integer, BufferedWriter> fileWriters = new HashMap<>();

	private static int fileIdx = 0;
	
	public String protocol;

	public HttpWas() {

	}

	public HttpWas(String programName, FunStore phi) {
		setServerFS(programName, phi);
	}

	public void setServerFS(String programName, FunStore phi) {
		if (!programFSMap.keySet().contains(programName)) {
			programFSMap.put(programName, phi);
		}
	}

	public void start() {
		try (ServerSocket srvSocket = new ServerSocket(PORT)) {
			while (true) {
				Socket conn = srvSocket.accept();
				InputStream input = conn.getInputStream();
				OutputStream output = conn.getOutputStream();

				// GET /rpc/programName HTTP/1.1\r\n
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
				String request = reader.readLine();

				// method와 version 사이에 있는 url(프로그램명)을 추출하기 위해 필요
				int idxMethod = request.indexOf(" ");
				int idxVersion = request.indexOf(" ", idxMethod + 1);
				String url = request.substring(idxMethod + 1, idxVersion);

				String urlProgramName = url.substring(5, url.length());

				if (programFSMap.keySet().contains(urlProgramName)) {
					// program에 대한 funstore가 정상적으로 등록되어 있는 경우
					// entity body까지 읽어들임
					String line;
					while (!(line = reader.readLine()).equals(""))
						;

					String sessionState = reader.readLine(); // sessionState -> OPEN_SESSION, sessionNum
					protocol = reader.readLine(); // protocol -> REQ, RET

					CSServer server;

					int session;

					if (sessionState.equals(OPEN_SESSION)) {
						session = newSession();

						FunStore phi = programFSMap.get(urlProgramName);
						server = new CSServer(phi, session, conn, reader, writer);

						sessionMap.put(count, server);

						Thread th = new Thread(() -> {
							server.run();
						});
						th.start();
					} else {
						session = Integer.parseInt(sessionState);

						server = sessionMap.get(session);

						server.connectClient(conn, reader, writer);

						synchronized (server.getLock()) {
							server.getLock().notify();
						}
					}
				} else {
					// program에 대한 funstore가 등록되지 않은 경우
					System.err.println("program funstore not found");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	private int newSession() {
		count = count + 1;

		return count;
	}

	class CSServer {
		private String[] libraryNames = { "primIsNothing_server", "primFromJust_server", "primOpenFile_server",
				"primCloseFile_server", "primWriteFile_server", "primReadFile_server", "primToString_server",
				"primToInt_server", "primToBool_server", "primReverse_server", "primAppend_server", "primLength_server",
				"primGetYear_server", "primGetMonth_server", "primGetDay_server", "primGetDate_server",
				"primGetHour_server", "primGetMinute_server",
				"primConnectDB", "primCreateTable", "primInsertRecord", "primUpdateRecord", "primDeleteRecord",
				"primQuery", "primFromRecord" };
		private ArrayList<String> libs = new ArrayList<>();

		private FunStore phi;
		private int sessionNum;

		private JSONParser jsonParser;

		private Socket conn;
		private BufferedReader reader;
		private BufferedWriter writer;

		private String lock;

		CSServer(FunStore phi, int sessionNum, Socket conn, BufferedReader reader, BufferedWriter writer)
				throws IOException {
			this.phi = phi;
			this.sessionNum = sessionNum;

			jsonParser = new JSONParser();

			this.conn = conn;
			this.reader = reader;
			this.writer = writer;

			Collections.addAll(libs, libraryNames);
			lock = "";
		}

		public String getLock() {
			return lock;
		}

		public void run() {
			handleClient();
		}

		public void connectClient(Socket socket, BufferedReader reader, BufferedWriter writer) {
			conn = socket;

			this.reader = reader;
			this.writer = writer;
		}

		private void handleClient() {
			try {
				if (protocol.equals(REQ)) {
					String cloFnInStr = reader.readLine();
					JSONObject cloFnInJson = (JSONObject) jsonParser.parse(cloFnInStr);
					Value cloFn = JSonUtil.fromJson(cloFnInJson);

					String numOfArgsInStr = reader.readLine();
					int numOfArgs = Integer.parseInt(numOfArgsInStr);
					ArrayList<Value> args = new ArrayList<>();

					for (int i = 0; i < numOfArgs; i++) {
						String argInStr = reader.readLine();
						JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
						Value arg = JSonUtil.fromJson(argInJson);

						args.add(arg);
					}

					String rStr = "r";
					Var rVar = new Var(rStr);

					Term reqTerm = new Let(rStr, new App(cloFn, args), rVar);

					evalServer(reqTerm, 0);

					reader.close();
					writer.close();

					sessionMap.remove(sessionNum);

				} else {
					System.err.println("Unexpected protocol(" + protocol + ")");
					writeHeader(400, "Bad Request");
					writer.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public void writeHeader(int code, String message) {
			try {
				writer.write("HTTP/1.1 " + code + " " + message + "\r\n");
				writer.write("Date: " + new Date() + "\r\n");
				writer.write("Server: " + "Apache 2.0\r\n\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void evalServer(Term m, int stackDepth) throws ParseException {
			while (true) {
				System.out.println("SERVER: " + m);

				if (m instanceof Let) {
					Let mLet = (Let) m;
					Term m1 = mLet.getT1();

					if (m1 instanceof App) {
						App mApp1 = (App) m1;

						if (mApp1.getFun() instanceof Clo) {
							Clo fClo = (Clo) mApp1.getFun();

							ClosedFun closedFun = lookup(phi, fClo.getF());
							Term letBody;

							ArrayList<String> zs = closedFun.getZs();

							if (!zs.isEmpty()) {
								String funName = zs.get(0);

								if (libs.contains(funName))
									letBody = evalLibrary(funName, mApp1.getWs());
								else
									letBody = SubstStaCS.substs(
											SubstStaCS.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
											closedFun.getXs(), mApp1.getWs());
							} else
								letBody = SubstStaCS.substs(
										SubstStaCS.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
										closedFun.getXs(), mApp1.getWs());

							m = new Let(mLet.getId(), letBody, mLet.getT2());
						}
					} else if (m1 instanceof Call) {
						Call mCall1 = (Call) m1;

						if (mCall1.getFun() instanceof Clo) {
							try {
								Clo fClo = (Clo) mCall1.getFun();
								ArrayList<Value> args = mCall1.getWs();

								// lock을 wait하기 전에 client에서 요청을 보내 wait하기 전에 notify를 할 수 있기 때문에
								// 하나의 연결되는 동작처럼 보내주고 wait을 해줘야됨
								synchronized (lock) {
									// Client로 Call을 날리는 부분
									writeHeader(200, "OK");
									writer.write(sessionNum + "\n");
									writer.write(CALL + "\n");
									writer.write(fClo.toJson() + "\n");
									writer.write(args.size() + "\n");
									for (Value arg : args) {
										writer.write(arg.toJson() + "\n");
									}
									writer.flush();

									// object wait 시키기
									lock.wait();
								}

								while (true) {
									if (protocol.equals(RET)) {
										String retValInStr = reader.readLine();
										JSONObject retValInJson = (JSONObject) jsonParser.parse(retValInStr);
										Value retVal = JSonUtil.fromJson(retValInJson);

										m = new Let(mLet.getId(), retVal, mLet.getT2());

										break;
									} else if (protocol.equals(REQ)) {
										String cloFnInStr = reader.readLine();
										JSONObject cloFnInJson = (JSONObject) jsonParser.parse(cloFnInStr);
										Value cloFn = JSonUtil.fromJson(cloFnInJson);

										String numOfArgsInStr = reader.readLine();
										int numOfArgs = Integer.parseInt(numOfArgsInStr);
										ArrayList<Value> cloFnArgs = new ArrayList<>();

										for (int i = 0; i < numOfArgs; i++) {
											String argInStr = reader.readLine();
											JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
											Value arg = JSonUtil.fromJson(argInJson);

											cloFnArgs.add(arg);
										}

										String rStr = "r";
										Var rVar = new Var(rStr);

										Term reqTerm = new Let(rStr, new App(cloFn, args), rVar);

										evalServer(reqTerm, stackDepth + 1);
									} else {
										throw new RuntimeException("evalServer(Call) Must not reach here. " + protocol);
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
								writeHeader(500, "Internal Server Error");
							} catch (InterruptedException e) {
								e.printStackTrace();
								writeHeader(500, "Internal Server Error");
							}
						}
					} else if (m1 instanceof Tapp) {
						Tapp mTapp1 = (Tapp) m1;

						if (mTapp1.getFun() instanceof Clo) {
							Clo fClo = (Clo) mTapp1.getFun();

							ClosedFun closedFun = lookup(phi, fClo.getF());

							m = new Let(mLet.getId(),
									SubstStaCS.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()), mLet.getT2());
						}
					} else if (m1 instanceof Clo) {
						Clo mClo1 = (Clo) m1;

						Term st = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mClo1);
						m = st;
					} else if (m1 instanceof Unit) {
						Unit mUnit1 = (Unit) m1;

						Term st = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mUnit1);
						m = st;
					} else if (m1 instanceof Num) {
						Num mNum1 = (Num) m1;

						Term st = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mNum1);
						m = st;
					} else if (m1 instanceof Str) {
						Str mStr1 = (Str) m1;

						Term st = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mStr1);
						m = st;
					} else if (m1 instanceof Bool) {
						Bool mBool1 = (Bool) m1;

						Term st = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mBool1);
						m = st;
					} else if (m1 instanceof Let) {
						Let mLet1 = (Let) m1;

						Let let = new Let(mLet1.getId(), mLet1.getT1(),
								new Let(mLet.getId(), mLet1.getT2(), mLet.getT2()));
						m = let;
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
				} else if (m instanceof Clo) {
					Clo mClo = (Clo) m;

					try {
						// REPLY
						synchronized (lock) {
							writeHeader(200, "OK");

							if (stackDepth == 0)
								writer.write(CLOSE_SESSION + "\n");
							else
								writer.write(sessionNum + "\n");

							writer.write(REPLY + "\n");
							writer.write(mClo.toJson() + "\n");
							writer.flush();

							if (stackDepth > 0)
								lock.wait();
						}

					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					return;
				} else if (m instanceof Unit) {
					Unit mUnit = (Unit) m;

					try {
						// REPLY
						synchronized (lock) {
							writeHeader(200, "OK");

							if (stackDepth == 0)
								writer.write(CLOSE_SESSION + "\n");
							else
								writer.write(sessionNum + "\n");

							writer.write(REPLY + "\n");
							writer.write(mUnit.toJson() + "\n");
							writer.flush();

							if (stackDepth > 0)
								lock.wait();
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					return;
				} else if (m instanceof Num) {
					Num mNum = (Num) m;

					try {
						// REPLY
						synchronized (lock) {
							writeHeader(200, "OK");

							if (stackDepth == 0)
								writer.write(CLOSE_SESSION + "\n");
							else
								writer.write(sessionNum + "\n");

							writer.write(REPLY + "\n");
							writer.write(mNum.toJson() + "\n");
							writer.flush();

							if (stackDepth > 0)
								lock.wait();
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					return;
				} else if (m instanceof Str) {
					Str mStr = (Str) m;

					try {
						// REPLY
						synchronized (lock) {
							writeHeader(200, "OK");

							if (stackDepth == 0)
								writer.write(CLOSE_SESSION + "\n");
							else
								writer.write(sessionNum + "\n");

							writer.write(REPLY + "\n");
							writer.write(mStr.toJson() + "\n");
							writer.flush();

							if (stackDepth > 0)
								lock.wait();
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					return;
				} else if (m instanceof Bool) {
					Bool mBool = (Bool) m;

					try {
						// REPLY
						synchronized (lock) {
							writeHeader(200, "OK");

							if (stackDepth == 0)
								writer.write(CLOSE_SESSION + "\n");
							else
								writer.write(sessionNum + "\n");

							writer.write(REPLY + "\n");
							writer.write(mBool.toJson() + "\n");
							writer.flush();

							if (stackDepth > 0)
								lock.wait();
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					return;
				}
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
		} else if (funName.equals("primFromJust_server")) {
			String content = ((Str) args.get(0)).getStr();
			
			return new Str(content);
		} else if (funName.equals("primOpenFile_server")) {
			String fileName = ((Str) args.get(0)).getStr();
			String mode = ((Str) args.get(1)).getStr();
			int idx = fileIdx;

			try {
				if (mode.equalsIgnoreCase("r")) {
					BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
					fileReaders.put(idx, fileReader);
				}
				else if (mode.equalsIgnoreCase("w")) {
					File file = new File(fileName);
					if (!file.exists()) {
						file.createNewFile();
					}
					BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName, true));
					fileWriters.put(idx, fileWriter);
				}
				else if (mode.equalsIgnoreCase("rw")) {
					File file = new File(fileName);
					if (!file.exists()) {
						file.createNewFile();
					}
					
					BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
					BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName, true));

					fileReaders.put(idx, fileReader);
					fileWriters.put(idx, fileWriter);
				}
				else
					throw new RuntimeException(mode + " is not support mode.");

				fileMap.put(idx, new Pair<>(fileName, mode));
				fileIdx = fileIdx + 1;

				return new Num(idx);
			} catch(FileNotFoundException e) {
				throw new RuntimeException(fileName + " is not found. Check file path.");
			} catch(IOException e) {
				throw new RuntimeException(fileName + " can not open.");
			}
		} else if (funName.equals("primCloseFile_server")) {
			int fileDesc = ((Num) args.get(0)).getI();
			// filename, mode
			Pair<String, String> fileInf = fileMap.get(fileDesc);
			String mode = fileInf.getValue();
			try {
				if (mode.equalsIgnoreCase("r")) {
					BufferedReader fileReader = fileReaders.remove(fileDesc);
					fileReader.close();
				}
				else if (mode.equalsIgnoreCase("w")) {
					BufferedWriter fileWriter = fileWriters.remove(fileDesc);
					fileWriter.close();
				}
				else if (mode.equalsIgnoreCase("rw")) {
					BufferedReader fileReader = fileReaders.remove(fileDesc);
					BufferedWriter fileWriter = fileWriters.remove(fileDesc);

					fileReader.close();
					fileWriter.close();
				}
				else
					throw new RuntimeException(fileDesc + " not support mode.");

				fileMap.remove(fileDesc);

				return new Unit();
			}
			catch (IOException e) {
				throw new RuntimeException(fileDesc + " closed.");
			}	
		} else if (funName.equals("primWriteFile_server")) {
			int fileDesc = ((Num) args.get(0)).getI();
			String content = ((Str) args.get(1)).getStr();

			Pair<String, String> fileInf = fileMap.get(fileDesc);
			String mode = fileInf.getValue();

			if (mode.equalsIgnoreCase("w") || mode.equalsIgnoreCase("rw")) {
				try {
					BufferedWriter fileWriter = fileWriters.get(fileDesc);
					fileWriter.write(content);
					fileWriter.flush();
				}
				catch (IOException e) {
					throw new RuntimeException(fileDesc + " occurs IOException.");
				}
			}
			else
				throw new RuntimeException(fileDesc + " not open write mode.");
			
			 return new Num(content.length());
		} else if (funName.equals("primReadFile_server")) {
			int fileDesc = ((Num) args.get(0)).getI();
			String ret = "";

			Pair<String, String> fileInf = fileMap.get(fileDesc);
			String mode = fileInf.getValue();
			
			if (mode.equalsIgnoreCase("r") || mode.equalsIgnoreCase("rw")) {
				try {
					BufferedReader fileReader = fileReaders.get(fileDesc);
					String line;
					
					while ((line = fileReader.readLine()) != null)
						ret += line;
				} catch(IOException e) {
					throw new RuntimeException(fileDesc + " occurs IOException.");
				}
				return new Str(ret);
			}
			else
				throw new RuntimeException(fileDesc + " not open reader mode.");
		} else if (funName.equals("primToString_server")) {
			String msg = ((Str) args.get(0)).getStr();
			
			return new Str(msg);
		} else if (funName.equals("primToInt_server")) {
			String msg = ((Str) args.get(0)).getStr();
			
			try {
				return new Num(Integer.parseInt(msg));
			} catch(NumberFormatException e) {
				throw new RuntimeException(msg + " is not Number format.");
			}
		} else if (funName.equals("primToBool_server")) {
			String msg = ((Str) args.get(0)).getStr();
			
			if (msg.equalsIgnoreCase("true"))
				return new Bool("True");
			else
				return new Bool("False");
		} else if (funName.equals("primReverse_server")) {
			String msg = ((Str) args.get(0)).getStr();
			String ret = new StringBuilder(msg).reverse().toString();
			
			return new Str(ret);
		} else if (funName.equals("primAppend_server")) {
			String msg1 = ((Str) args.get(0)).getStr();
			String msg2 = ((Str) args.get(1)).getStr();
			
			return new Str(msg1 + msg2);
		} else if (funName.equals("primLength_server")) {
			String msg = ((Str) args.get(0)).getStr();
			
			return new Num(msg.length());
		} else if (funName.equals("primGetYear_server")) {
			Calendar calendar = Calendar.getInstance();
			
			return new Num(calendar.get(Calendar.YEAR));
			
		} else if (funName.equals("primGetMonth_server")) {
			Calendar calendar = Calendar.getInstance();
			
			return new Num(calendar.get(Calendar.MONTH));
			
		} else if (funName.equals("primGetDay_server")) {
			Calendar calendar = Calendar.getInstance();
			
			return new Num(calendar.get(Calendar.DAY_OF_WEEK));
			
		} else if (funName.equals("primGetDate_server")) {
			Calendar calendar = Calendar.getInstance();
			
			return new Num(calendar.get(Calendar.DAY_OF_MONTH));
			
		} else if (funName.equals("primGetHour_server")) {
			Calendar calendar = Calendar.getInstance();
			
			return new Num(calendar.get(Calendar.HOUR_OF_DAY));
		} else if (funName.equals("primGetMinute_server")) {
			Calendar calendar = Calendar.getInstance();
			
			return new Num(calendar.get(Calendar.MINUTE));
		} else
			return evalDBLibrary(funName, args);
	}
	
	public static Term evalDBLibrary(String funName, ArrayList<Value> args) {
		if (funName.equals("primConnectDB")) {
			String ip = ((Str) args.get(0)).getStr();
			int port = ((Num) args.get(1)).getI();
			String id = ((Str) args.get(2)).getStr();
			String pwd = ((Str) args.get(3)).getStr();
			String dbname = ((Str) args.get(4)).getStr();
			
			
		}
		else if (funName.equals("primCreateTable")) {
			String tblname = ((Str) args.get(0)).getStr();
			String recordField = ((Str) args.get(1)).getStr();
			
			
		}
		else if (funName.equals("primInsertRecord")) {
			String tblname = ((Str) args.get(0)).getStr();
			String content = ((Str) args.get(1)).getStr();
			
			
		}
		else if (funName.equals("primUpdateRecord")) {
			String tblname = ((Str) args.get(0)).getStr();
			String content = ((Str) args.get(1)).getStr();
			
		}
		else if (funName.equals("primDeleteRecord")) {
			String tblname = ((Str) args.get(0)).getStr();
			int recordId = ((Num) args.get(1)).getI();
			
		}
		else if (funName.equals("primQuery" )) {
			String tblname = ((Str) args.get(0)).getStr();
			int recordId = ((Num) args.get(1)).getI();
			String field = ((Str) args.get(2)).getStr();
			
			
		}
		else if (funName.equals("primFromRecord")) {
			String tblname = ((Str) args.get(0)).getStr();
			int recordId = ((Num) args.get(1)).getI();
		}
		else
			throw new RuntimeException(funName + " is not support Library.");
		
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