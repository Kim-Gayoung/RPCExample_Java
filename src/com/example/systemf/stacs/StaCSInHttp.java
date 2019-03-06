package com.example.systemf.stacs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Function;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.systemf.CompException;
import com.example.systemf.TyEnv;
import com.example.systemf.TypeCheckException;
import com.example.systemf.TypeChecker;
import com.example.systemf.ast.Type;
import com.example.systemf.parser.Parser;
import com.example.systemf.sta.ast.App;
import com.example.systemf.sta.ast.Bool;
import com.example.systemf.sta.ast.Call;
import com.example.systemf.sta.ast.Clo;
import com.example.systemf.sta.ast.If;
import com.example.systemf.sta.ast.Let;
import com.example.systemf.sta.ast.Num;
import com.example.systemf.sta.ast.PrimTerm;
import com.example.systemf.sta.ast.Req;
import com.example.systemf.sta.ast.Ret;
import com.example.systemf.sta.ast.Str;
import com.example.systemf.sta.ast.Term;
import com.example.systemf.sta.ast.Unit;
import com.example.systemf.sta.ast.Value;
import com.example.systemf.sta.ast.Var;
import com.example.systemf.starpc.CompStaRpc;
import com.example.utils.TripleTup;

public class StaCSInHttp {
	private static final String OPEN_SESSION = "OPEN_SESSION";
	private static final String CLOSE_SESSION = "CLOSE_SESSION";
	
	private static final String REQ = "REQ";
	private static final String RET = "RET";
	private static final String REPLY = "REPLY";
	private static final String CALL = "CALL";
	
	private static final int PORT = 8080;
	
	public static void main(String[] args) throws TypeCheckException, CompException {
		Parser parser;
		String serverAddr = "127.0.0.1";
		HttpWas was = new HttpWas();

		Thread wasThread = new Thread(() -> {
			was.start();
		});

		wasThread.start();

		while (true) {
			try {
				parser = new Parser();
				System.out.print("Enter a file name: ");
				String fileName = new Scanner(System.in).next();

				FileReader fileReader = new FileReader("./testcase/" + fileName);
				Scanner scan = new Scanner(fileReader);

				while (scan.hasNext()) {
					System.out.println(scan.nextLine());
				}
				System.out.println();

				fileReader = new FileReader("./testcase/" + fileName);

				com.example.systemf.ast.Term rpcProgram = parser.Parsing(fileReader);
				Type rpcProgramTy = TypeChecker.check(rpcProgram, new TyEnv());
				Term staRpcProgram = CompStaRpc.compStaRpc(rpcProgram);

				TripleTup<Term, FunStore, FunStore> staCsTerm = CompStaCs.cloConv(staRpcProgram, new ArrayList<>());

				Term mainExpr = staCsTerm.getFirst();
				FunStore clientFS = staCsTerm.getSecond();
				FunStore serverFS = staCsTerm.getThird();

				System.out.println("Client FunStore: " + clientFS);
				System.out.println("Server FunStore: " + serverFS);

				String programName = fileName.substring(0, fileName.indexOf("."));

				was.setServerFS(programName, serverFS);

				Thread clientThread = new Thread(() -> {
					CSClient client = new CSClient(serverAddr, programName, clientFS);
					Value result = client.evalClient(mainExpr);

					System.out.println("result: " + result);
				});

				clientThread.start();
				clientThread.join();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (LexerException e) {
				e.printStackTrace();
			}
			catch (ParserException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static class HttpWas {
		private static HashMap<String, FunStore> programFSMap = new HashMap<>();
		private static HashMap<Integer, CSServer> sessionMap = new HashMap<>();
		private static int count = 0;

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
						}
						else {
							session = Integer.parseInt(sessionState);

							server = sessionMap.get(session);

							server.connectClient(conn, reader, writer);

							synchronized (server.getLock()) {
								server.getLock().notify();
							}
						}
					}
					else {
						// program에 대한 funstore가 등록되지 않은 경우
						System.err.println("program funstore not found");
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		private int newSession() {
			count = count + 1;

			return count;
		}

		class CSServer {
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

					}
					else {
						System.err.println("Unexpected protocol(" + protocol + ")");
						writeHeader(400, "Bad Request");
						writer.flush();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (ParseException e) {
					e.printStackTrace();
				}
			}

			public void writeHeader(int code, String message) {
				try {
					writer.write("HTTP/1.1 " + code + " " + message + "\r\n");
					writer.write("Date: " + new Date() + "\r\n");
					writer.write("Server: " + "Apache 2.0\r\n\r\n");
				}
				catch (IOException e) {
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
								Let let = new Let(mLet.getId(),
										SubstStaCS.substs(
												SubstStaCS.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
												closedFun.getXs(), mApp1.getWs()),
										mLet.getT2());

								m = let;
							}
						}
						else if (m1 instanceof Call) {
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
										}
										else if (protocol.equals(REQ)) {
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
										}
										else {
											throw new RuntimeException(
													"evalServer(Call) Must not reach here. " + protocol);
										}
									}
								}
								catch (IOException e) {
									e.printStackTrace();
									writeHeader(500, "Internal Server Error");
								}
								catch (InterruptedException e) {
									e.printStackTrace();
									writeHeader(500, "Internal Server Error");
								}
							}
						}
						else if (m1 instanceof Clo) {
							Clo mClo1 = (Clo) m1;

							Term st = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mClo1);
							m = st;
						}
						else if (m1 instanceof Num) {
							Num mConst1 = (Num) m1;

							Term st = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mConst1);
							m = st;
						}
						else if (m1 instanceof Let) {
							Let mLet1 = (Let) m1;

							Let let = new Let(mLet1.getId(), mLet1.getT1(),
									new Let(mLet.getId(), mLet1.getT2(), mLet.getT2()));
							m = let;
						}

					}
					else if (m instanceof Clo) {
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

						}
						catch (IOException e) {
							e.printStackTrace();
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}

						return;
					}
					else if (m instanceof Num) {
						Num mConst = (Num) m;

						try {
							// REPLY
							synchronized (lock) {
								writeHeader(200, "OK");

								if (stackDepth == 0)
									writer.write(CLOSE_SESSION + "\n");
								else
									writer.write(sessionNum + "\n");

								writer.write(REPLY + "\n");
								writer.write(mConst.toJson() + "\n");
								writer.flush();

								if (stackDepth > 0)
									lock.wait();
							}
						}
						catch (IOException e) {
							e.printStackTrace();
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}

						return;
					}
				}
			}
		}
	}

	public static class CSClient {
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

			this.serverAddr = serverAddr;
		}

		private void connectServer() {
			if (socket == null || socket.isClosed()) {
				try {
					socket = new Socket(serverAddr, PORT);

					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				}
				catch (UnknownHostException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
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
			}
			catch (IOException e) {
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
							}
							else {
								sessionNum = Integer.parseInt(sessionState);
							}

							if (protocol.equals(REPLY)) {
								String strReply = reader.readLine();
								JSONObject replyJson = (JSONObject) jsonParser.parse(strReply);
								Value replyVal = JSonUtil.fromJson(replyJson);

								retM = new Let(mLet.getId(), replyVal, mLet.getT2());
							}
							else if (protocol.equals(CALL)) {
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
							}
							else {
								System.err.println("receiver: Unexpected protocol(" + protocol + ")");
								retM = null;
							}
						}
						catch (NumberFormatException e) {
							e.printStackTrace();
							retM = null;
						}
						catch (ParseException e) {
							e.printStackTrace();
							retM = null;
						}
					}
					else {
						System.err.println(statusCode);
						retM = null;
					}

				}
				catch (IOException e) {
					e.printStackTrace();
					retM = null;
				}

				try {
					socket.close();
				}
				catch (IOException e) {
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

						if (mApp1.getFun() instanceof Clo) {
							Clo fClo = (Clo) mApp1.getFun();

							ClosedFun closedFun = lookup(clientFS, fClo.getF());

							m = new Let(mLet.getId(),
									SubstStaCS.substs(
											SubstStaCS.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
											closedFun.getXs(), mApp1.getWs()),
									mLet.getT2());
						}
					}
					else if (m1 instanceof Req) {
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
							}
							catch (IOException e) {
								e.printStackTrace();
							}

							m = receiver.apply(mLet);
						}
					}
					else if (m1 instanceof Clo) {
						Clo mClo1 = (Clo) m1;

						m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mClo1);
					}
					else if (m1 instanceof Unit) {
						Unit mUnit1 = (Unit) m1;
						
						m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mUnit1);
					}
					else if (m1 instanceof Num) {
						Num mNum1 = (Num) m1;

						m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mNum1);
					}
					else if (m1 instanceof Bool) {
						Bool mBool1 = (Bool) m1;
						
						m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mBool1);
					}
					else if (m1 instanceof Str) {
						Str mStr1 = (Str) m1;
						
						m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), mStr1);
					}
					else if (m1 instanceof Var) {
						Var mVar1 = (Var) m1;
						
						// append와 같은 Library 함수?
					}
					else if (m1 instanceof Let) {
						Let mLet1 = (Let) m1;

						Let let = new Let(mLet1.getId(), mLet1.getT1(),
								new Let(mLet.getId(), mLet1.getT2(), mLet.getT2()));

						m = let;
					}
					else if (m1 instanceof Ret) {
						Ret mRet1 = (Ret) m1;
						Value retVal = mRet1.getW();

						try {
							writeHeader();
							writer.write(sessionNum + "\n"); // RET의 경우 sessionNu이 null인 상태는 있을 수가 없음
							writer.write(RET + "\n");
							writer.write(retVal.toJson() + "\n");
							
							writer.flush();
						}
						catch (IOException e) {
							e.printStackTrace();
						}

						m = receiver.apply(mLet);
					}
					else if (m1 instanceof If) {
						If mIf1 = (If) m1;
						
						Term cond = mIf1.getCond();
						
						if (cond instanceof Bool) {
							Bool boolCond = (Bool) cond;
							
							if (boolCond.getBool())
								m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), (Value) mIf1.getThenT());
							else
								m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), (Value) mIf1.getElseT());
						}
					}
					else if (m1 instanceof PrimTerm) {
						PrimTerm mExpr1 = (PrimTerm) m1;
						Term oprnd1;
						Term oprnd2;
						Value v = null;
						
						switch(mExpr1.getOp()) {
						case 0:			// ADD
						case 1:			// SUB
						case 2:			// MUL
						case 3:			// DIV
							oprnd1 = mExpr1.getOprnds().get(0);
							oprnd2 = mExpr1.getOprnds().get(1);
							
							if (oprnd1 instanceof Num && oprnd2 instanceof Num) {
								Num numOprnd1 = (Num) oprnd1;
								Num numOprnd2 = (Num) oprnd2;
								
								v = compPrimTerm(numOprnd1, mExpr1.getOp(), numOprnd2);
							}
							else
								throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") and oprnd2(" + oprnd2 + ") are not Num.");
							break;
						case 4:		// UNARY MINUS
							oprnd1 = mExpr1.getOprnds().get(0);
							
							if (oprnd1 instanceof Num) {
								Num numOprnd1 = (Num) oprnd1;
								
								v = new Num(-numOprnd1.getI());
							}
							else
								throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") is not Num.");
							break;
						case 5:			// GTHAN
						case 6:			// GEQUAL
						case 7:			// LTHAN
						case 8:			// LEQUAL
							oprnd1 = mExpr1.getOprnds().get(0);
							oprnd2 = mExpr1.getOprnds().get(1);
							
							if (oprnd1 instanceof Num && oprnd2 instanceof Num) {
								Num numOprnd1 = (Num) oprnd1;
								Num numOprnd2 = (Num) oprnd2;
								
								v = compPrimTerm(numOprnd1, mExpr1.getOp(), numOprnd2);
							}
							else
								throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") and oprnd2(" + oprnd2 + ") are not Num.");
							break;
						case 9:			// EQUAL
							oprnd1 = mExpr1.getOprnds().get(0);
							oprnd2 = mExpr1.getOprnds().get(1);
							
							if (oprnd1.getClass() == oprnd2.getClass()) {
								v = new Bool(Boolean.toString(oprnd1.equals(oprnd2)));
							}
							else
								throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") and oprnd2(" + oprnd2 + ") are not Equal.");
							break;
						case 10:		// NOTEQUAL
							oprnd1 = mExpr1.getOprnds().get(0);
							oprnd2 = mExpr1.getOprnds().get(1);
							
							if (oprnd1.getClass() == oprnd2.getClass()) {
								v = new Bool(Boolean.toString(!oprnd1.equals(oprnd2)));
							}
							else
								throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") and oprnd2(" + oprnd2 + ") are not Equal.");
							break;
						case 11:
						case 12:
							oprnd1 = mExpr1.getOprnds().get(0);
							oprnd2 = mExpr1.getOprnds().get(1);
							
							if (oprnd1 instanceof Bool && oprnd2 instanceof Bool) {
								Bool boolOprnd1 = (Bool) oprnd1;
								Bool boolOprnd2 = (Bool) oprnd2;
								
								v = compPrimTerm(boolOprnd1, mExpr1.getOp(), boolOprnd2);
							}
							else
								throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") and oprnd2(" + oprnd2 + ") are not Bool.");
							break;
						case 13:
							oprnd1 = mExpr1.getOprnds().get(0);
							
							if (oprnd1 instanceof Bool) {
								Bool boolOprnd1 = (Bool) oprnd1;
								
								v = new Bool(Boolean.toString(!boolOprnd1.getBool()));
							}
							else
								throw new RuntimeException("StaCsInHttp.evalClient(PrimTerm): oprnd1(" + oprnd1 + ") is not Bool.");
							break;
						}
						
						m = SubstStaCS.subst(mLet.getT2(), mLet.getId(), v);
					}
				}
				else if (m instanceof Clo || m instanceof Unit || m instanceof Num || m instanceof Str || m instanceof Bool) {
					return (Value) m;
				}
				else {
					throw new RuntimeException("StaCsInHttp.evalClient: Must not reach here");
				}
			}
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
