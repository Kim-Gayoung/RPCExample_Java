package com.example.rpc;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.example.lib.LexerException;
import com.example.lib.ParserException;

public class Main {
	public static Value eval(Term m, Location loc) {
		if (m instanceof Lam) {
			Lam lam = (Lam) m;
			return lam;
		}
		else if (m instanceof App) {
			App app = (App) m;
			Lam lam = (Lam) eval(app.getFun(), loc);
			Value w = eval(app.getArg(), loc);
			Value v = eval(subst(lam.getM(), lam.getX(), w), lam.getLoc());

			return v;
		}
		else if (m instanceof Num) {
			Num con = (Num) m;

			return con;
		}
		else if (m instanceof Let) {

		}
		else {
			return null;
		}
	}

	public static Term subst(Term m, String x, Value v) {
		if (m instanceof Num) {
			Num con = (Num) m;

			return con;
		}
		else if (m instanceof Var) {
			Var var = (Var) m;

			if (var.getVar().equals(x)) {
				return v;
			}
			else {
				return var;
			}
		}
		else if (m instanceof Bool) {
			Bool bool = (Bool) m;

			return bool;
		}
		else if (m instanceof Str) {
			Str str = (Str) m;

			return str;
		}
		else if (m instanceof Unit) {
			Unit unit = (Unit) m;

			return unit;
		}
		else if (m instanceof Lam) {
			Lam lam = (Lam) m;

			if (lam.getX().equals(x)) {
				return lam;
			}
			else {
				Lam l = lam;
				l.setM(subst(lam.getM(), x, v));

				return l;
			}
		}
		else if (m instanceof App) {
			App app = (App) m;

			Term left = subst(app.getFun(), x, v);
			Term right = subst(app.getArg(), x, v);
			App ret = new App(left, right);

			return ret;
		}
		else if (m instanceof Let) {
			Let let = (Let) m;
			Let ret;

			Var id = (Var) subst(let.getId(), x, v);
			Term t1 = subst(let.getT1(), x, v);

			if (let.getT2() != null) {
				Term t2 = subst(let.getT2(), x, v);
				ret = new Let(id, t1, t2);
			}
			else
				ret = new Let(id, t1);

			return ret;
		}
		else if (m instanceof If) {
			If mIf = (If) m;
			If ret;

			Cond cond = (Cond) subst(mIf.getCond(), x, v);
			Term thenT = subst(mIf.getThenT(), x, v);
			Term elseT = subst(mIf.getElseT(), x, v);

			ret = new If(cond, thenT, elseT);

			return ret;
		}
		else if (m instanceof Arithmetic) {
			Arithmetic arith = (Arithmetic) m;
			Arithmetic ret;
			
			Term oprnd1 = subst(arith.getOprnd1(), x, v);
			if (arith.getOprnd2() != null) {
				Term oprnd2 = subst(arith.getOprnd2(), x, v);
				ret = new Arithmetic(oprnd1, arith.getOp(), oprnd2);
			}
			else
				ret = new Arithmetic(oprnd1, arith.getOp());
			
			return ret; 
		}
		else if (m instanceof Logical) {
			Logical logic = (Logical) m;

			Term oprnd1 = subst(logic.getOprnd1(), x, v);
			Term oprnd2 = subst(logic.getOprnd2(), x, v);

			return new Logical(oprnd1, logic.getOp(), oprnd2);
		}
		else if (m instanceof Comp) {
			Comp comp = (Comp) m;

			Term oprnd1 = subst(comp.getOprnd1(), x, v);
			Term oprnd2 = subst(comp.getOprnd2(), x, v);

			return new Comp(oprnd1, comp.getOp(), oprnd2);
		}
		else if (m instanceof Params) {
			Params params = (Params) m;
			Params ret;
			
			if (params.getId() != null) {
				Var id = (Var) subst(params.getId(), x, v);
				
				if (params.getIds() != null) {
					Params ids = (Params) subst(params.getIds(), x, v);
					ret = new Params(id, ids);
				}
				else
					ret = new Params(id);
				
				return ret;
			}
			else				// Lambda 식의 Parameter가 아무것도 없는 경우 -> 대치할 것이 없음
				return params;
		}
		else
			return null;
	}

	public static void main(String[] args) throws ParserException, IOException, LexerException {
		// LexicalAnalyzer lexical = new LexicalAnalyzer(new
		// InputStreamReader(System.in));
		Parser parser = new Parser();

		System.out.println("1: File, the other: Console");
		System.out.print("Enter the number: ");
		String select = new Scanner(System.in).next();

		Term ex1;

		if (select.equals("1")) {
			System.out.print("Enter a file name: ");
			String fileName = new Scanner(System.in).next();

			FileReader fileReader = new FileReader("./testcase/" + fileName);
			Scanner scan = new Scanner(fileReader);

			while (scan.hasNext()) {
				System.out.println(scan.nextLine());
			}
			System.out.println();

			fileReader = new FileReader("./testcase/" + fileName);
			ex1 = parser.Parsing(fileReader);
		}
		else {
			ex1 = parser.Parsing(new InputStreamReader(System.in));
		}
		
		System.out.println(ex1.toString());

//		System.out.println("start...");
//
//		System.out.println(ex1.toString());
//		System.out.println(eval(ex1, Location.Client).toString());
//
//		com.example.typedrpc.TypedTerm tym = Infer.infer(ex1);
//		System.out.println(tym.toString());
//
//		System.out.println("----RPC EncTerm----");
//		com.example.encrpc.EncTerm encTerm = CompRPCEncTerm.compEncTerm(tym);
//		System.out.println(encTerm);
//		System.out.println(TypedRPCEnc.eval(encTerm));
//		System.out.println("----RPC StaTerm----");
//		com.example.starpc.StaTerm staTerm = CompRPCStaTerm.compStaTerm(tym);
//		System.out.println(staTerm);
//		System.out.println(TypedRPCSta.eval(staTerm));
//
//		System.out.println("In Encoding CS: ");
//		TripleTup<EncTerm, com.example.enccs.FunStore, com.example.enccs.FunStore> csEncTerm = CompCSEncTerm
//				.compCSEncTerm(encTerm);
//		System.out.println("----CS EncTerm----");
//		System.out.println("client function store: ");
//		System.out.println(csEncTerm.getSecond());
//		System.out.println("server function store: ");
//		System.out.println(csEncTerm.getThird());
//
//		System.out.println("main client expression: ");
//		System.out.println(csEncTerm.getFirst().toString());
//		System.out.println("evaluates to ");
//		com.example.enccs.EncTerm csencv = TypedCSEnc.eval(csEncTerm.getSecond(), csEncTerm.getThird(),
//				csEncTerm.getFirst());
//		System.out.println(csencv);
//
//		System.out.println("In Stateful CS: ");
//		TripleTup<com.example.stacs.StaTerm, com.example.stacs.FunStore, com.example.stacs.FunStore> csStaTerm = CompCSStaTerm
//				.compCSStaTerm(staTerm);
//		System.out.println("----CS StaTerm----");
//		System.out.println("client function store: ");
//		System.out.println(csStaTerm.getSecond());
//		System.out.println("server function store: ");
//		System.out.println(csStaTerm.getThird());
//
//		System.out.println("main client expression: ");
//		System.out.println(csStaTerm.getFirst().toString());
//		System.out.println("evaluates to ");
//		com.example.stacs.StaTerm csstav = TypedCSSta.eval(csStaTerm.getSecond(), csStaTerm.getThird(),
//				csStaTerm.getFirst());
//		System.out.println(csstav);
	}
}
