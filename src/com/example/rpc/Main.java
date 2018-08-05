package com.example.rpc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import com.example.enccs.CompCSEncTerm;
import com.example.enccs.EncTerm;
import com.example.enccs.TypedCSEnc;
import com.example.encrpc.CompRPCEncTerm;
import com.example.encrpc.TypedRPCEnc;
import com.example.stacs.CompCSStaTerm;
import com.example.stacs.TypedCSSta;
import com.example.starpc.CompRPCStaTerm;
import com.example.starpc.TypedRPCSta;
import com.example.typedrpc.Infer;
import com.example.utils.TripleTup;
import com.rpc.parser.Expr;
import com.rpc.parser.LexerException;
import com.rpc.parser.LexicalAnalyzer;
import com.rpc.parser.Parser;
import com.rpc.parser.ParserException;

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
		else if (m instanceof Const) {
			Const con = (Const) m;

			return con;
		}
		else {
			return null;
		}
	}

	public static Term subst(Term m, String x, Value v) {
		if (m instanceof Var) {
			Var var = (Var) m;

			if (var.getVar().equals(x)) {
				return v;
			}
			else {
				return var;
			}
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
		else if (m instanceof Const) {
			Const con = (Const) m;

			return con;
		}
		else
			return null;
	}

	public static void main(String[] args) throws ParserException, IOException, LexerException {
		StringReader sr = new StringReader("(lam^s f. (lam^s x. x) (f 1)) (lam^c y. (lam^s z. z) y)");
		LexicalAnalyzer lexical = new LexicalAnalyzer(new InputStreamReader(System.in));
		Parser parser = new Parser(lexical);
		
		Term ex1 = parser.Parsing();
		
//		Term leftApp = new App(new Lam(Location.Server, "x", new Var("x")), new App(new Var("f"), new Const(1)));
//
//		Term left = new Lam(Location.Server, "f", leftApp);
//
//		Term right = new Lam(Location.Client, "y", new App(new Lam(Location.Server, "z", new Var("z")), new Var("y")));
//
//		Term ex1 = new App(left, right);
		System.out.println(ex1.toString());
		System.out.println(eval(ex1, Location.Client).toString());

		com.example.typedrpc.TypedTerm tym = Infer.infer(ex1);
		System.out.println(tym.toString());
		
		System.out.println("----RPC EncTerm----");
		com.example.encrpc.EncTerm encTerm = CompRPCEncTerm.compEncTerm(tym);
		System.out.println(encTerm);
		System.out.println(TypedRPCEnc.eval(encTerm));
		System.out.println("----RPC StaTerm----");
		com.example.starpc.StaTerm staTerm = CompRPCStaTerm.compStaTerm(tym);
		System.out.println(staTerm);
		System.out.println(TypedRPCSta.eval(staTerm));
		
		System.out.println("In Stateful CS: ");
		TripleTup<com.example.stacs.StaTerm, com.example.stacs.FunStore, com.example.stacs.FunStore> csStaTerm = CompCSStaTerm.compCSStaTerm(staTerm);
		System.out.println("----CS StaTerm----");
		System.out.println("client function store: ");
		System.out.println(csStaTerm.getSecond());
		System.out.println("server function store: ");
		System.out.println(csStaTerm.getThird());
		
		System.out.println("main client expression: ");
		System.out.println(csStaTerm.getFirst().toString());
		System.out.println("evaluates to ");
		com.example.stacs.StaTerm csstav = TypedCSSta.eval(csStaTerm.getSecond(), csStaTerm.getThird(), csStaTerm.getFirst());
		System.out.println(csstav);
		
		System.out.println("In Encoding CS: ");
		TripleTup<EncTerm, com.example.enccs.FunStore, com.example.enccs.FunStore> csEncTerm = CompCSEncTerm.compCSEncTerm(encTerm);
		System.out.println("----CS EncTerm----");
		System.out.println("client function store: ");
		System.out.println(csEncTerm.getSecond());
		System.out.println("server function store: ");
		System.out.println(csEncTerm.getThird());
		
		System.out.println("main client expression: ");
		System.out.println(csEncTerm.getFirst().toString());
		System.out.println("evaluates to ");
		com.example.enccs.EncTerm csencv = TypedCSEnc.eval(csEncTerm.getSecond(), csEncTerm.getThird(), csEncTerm.getFirst());
		System.out.println(csencv);
	}
}
