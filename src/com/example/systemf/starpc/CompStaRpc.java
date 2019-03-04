package com.example.systemf.starpc;

import java.util.ArrayList;

import com.example.systemf.CompException;
import com.example.systemf.ast.LocType;
import com.example.systemf.ast.Location;
import com.example.systemf.sta.ast.App;
import com.example.systemf.sta.ast.Bool;
import com.example.systemf.sta.ast.Call;
import com.example.systemf.sta.ast.ExprTerm;
import com.example.systemf.sta.ast.If;
import com.example.systemf.sta.ast.Lam;
import com.example.systemf.sta.ast.Let;
import com.example.systemf.sta.ast.Num;
import com.example.systemf.sta.ast.Req;
import com.example.systemf.sta.ast.Ret;
import com.example.systemf.sta.ast.Str;
import com.example.systemf.sta.ast.Tapp;
import com.example.systemf.sta.ast.Term;
import com.example.systemf.sta.ast.Tylam;
import com.example.systemf.sta.ast.Unit;
import com.example.systemf.sta.ast.Value;
import com.example.systemf.sta.ast.Var;

import javafx.util.Pair;

public class CompStaRpc {
	private static int i = 1;

	public static Term compStaRpc(com.example.systemf.ast.Term term) throws CompException {
		return compClient(term);
	}

	public static Term compClient(com.example.systemf.ast.Term t) throws CompException {
		if (t instanceof com.example.systemf.ast.Unit) {
			return new Unit();
		}
		else if (t instanceof com.example.systemf.ast.Num) {
			com.example.systemf.ast.Num tNum = (com.example.systemf.ast.Num) t;

			return new Num(tNum.getI());
		}
		else if (t instanceof com.example.systemf.ast.Str) {
			com.example.systemf.ast.Str tStr = (com.example.systemf.ast.Str) t;

			return new Str(tStr.getStr());
		}
		else if (t instanceof com.example.systemf.ast.Bool) {
			com.example.systemf.ast.Bool tBool = (com.example.systemf.ast.Bool) t;

			return new Bool(tBool.isBool());
		}
		else if (t instanceof com.example.systemf.ast.Var) {
			com.example.systemf.ast.Var tVar = (com.example.systemf.ast.Var) t;

			return new Var(tVar.getVar());
		}
		else if (t instanceof com.example.systemf.ast.Lam) {
			com.example.systemf.ast.Lam tLam = (com.example.systemf.ast.Lam) t;
			Term term;

			if (tLam.getLoc() == Location.Client)
				term = compClient(tLam.getM());
			else
				term = compServer(tLam.getM());

			return new Lam(tLam.getLoc(), tLam.getX(), tLam.getIdTy(), term);
		}
		else if (t instanceof com.example.systemf.ast.Tylam) {
			com.example.systemf.ast.Tylam tTylam = (com.example.systemf.ast.Tylam) t;
			Term term = compClient(tTylam.getTerm());

			return new Tylam(tTylam.getTy(), term);
		}
		else if (t instanceof com.example.systemf.ast.App) {
			com.example.systemf.ast.App tApp = (com.example.systemf.ast.App) t;

			String fvar = "f" + i++;
			String xvar = "x" + i++;
			String rvar = "r" + i++;

			Var f = new Var(fvar);
			Var x = new Var(xvar);
			Var r = new Var(rvar);

			Term fun = compClient(tApp.getFun());
			Term arg = compClient(tApp.getArg());

			ArrayList<Value> ws = new ArrayList<>();
			ws.add(x);

			if (tApp.getLoc() instanceof LocType) {
				Location loc = ((LocType) tApp.getLoc()).getLoc();

				if (loc == Location.Client)
					return new Let(fvar, fun, new Let(xvar, arg, new Let(rvar, new App(f, ws), r)));
				else {
					return new Let(fvar, fun, new Let(xvar, arg, new Let(rvar, new Req(f, ws), r)));
				}
			}
			else
				throw new CompException("CompStaRpc(compClient) TypedLocation is not LocType" + tApp.getLoc());
		}
		else if (t instanceof com.example.systemf.ast.Tapp) {
			com.example.systemf.ast.Tapp tTApp = (com.example.systemf.ast.Tapp) t;

			String fvar = "f" + i++;
			String rvar = "r" + i++;

			Var f = new Var(fvar);
			Var r = new Var(rvar);

			Term fun = compClient(tTApp.getFun());

			return new Let(fvar, fun, new Let(rvar, new Tapp(f, tTApp.getTy()), r));
		}
		else if (t instanceof com.example.systemf.ast.Let) {
			com.example.systemf.ast.Let tLet = (com.example.systemf.ast.Let) t;

			Term term1 = compClient(tLet.getT1());
			Term term2 = compClient(tLet.getT2());

			return new Let(tLet.getId(), tLet.getIdTy(), term1, term2);
		}
		else if (t instanceof com.example.systemf.ast.If) {
			com.example.systemf.ast.If tIf = (com.example.systemf.ast.If) t;

			Term condTerm = compClient(tIf.getCond());
			Term thenTerm = compClient(tIf.getThenT());
			Term elseTerm = compClient(tIf.getElseT());

			String condvar = "c" + i++;
			String thenvar = "t" + i++;
			String elsevar = "e" + i++;
			String rvar = "r" + i++;

			Var condv = new Var(condvar);
			Var thenv = new Var(thenvar);
			Var elsev = new Var(elsevar);
			Var r = new Var(rvar);

			return new Let(condvar, condTerm, new Let(thenvar, thenTerm,
					new Let(elsevar, elseTerm, new Let(rvar, new If(condv, thenv, elsev), r))));
		}
		else if (t instanceof com.example.systemf.ast.ExprTerm) {
			com.example.systemf.ast.ExprTerm tExpr = (com.example.systemf.ast.ExprTerm) t;

			ArrayList<Term> oprnds = new ArrayList<>();
			ArrayList<Pair<String, Term>> pairOprnds = new ArrayList<>();

			// pairOprnds { (x2, oprnd2), (x1, oprnd1) }
			for (com.example.systemf.ast.Term oprnd : tExpr.getOprnds()) {
				Term compOprnd = compClient(oprnd);

				String xvar = "x" + i++;
				Pair<String, Term> p = new Pair<>(xvar, compOprnd);
				pairOprnds.add(0, p);
			}

			// oprnds { x1, x2 }
			for (Pair<String, Term> p : pairOprnds) {
				oprnds.add(0, new Var(p.getKey()));
			}

			String rvar = "r" + i++;
			Var r = new Var(rvar);

			Term expr = new Let(rvar, new ExprTerm(oprnds, tExpr.getOp()), r);

			for (int i = 0; i < pairOprnds.size(); i++) {
				Pair<String, Term> p = pairOprnds.get(i);
				expr = new Let(p.getKey(), p.getValue(), expr);
			}

			return expr;
		}
		else
			throw new CompException("CompStaRpc(compClient) not match " + t.getClass() + "(" + t + ")");
	}

	public static Term compServer(com.example.systemf.ast.Term t) throws CompException {
		if (t instanceof com.example.systemf.ast.Unit) {
			return new Unit();
		}
		else if (t instanceof com.example.systemf.ast.Num) {
			com.example.systemf.ast.Num tNum = (com.example.systemf.ast.Num) t;

			return new Num(tNum.getI());
		}
		else if (t instanceof com.example.systemf.ast.Str) {
			com.example.systemf.ast.Str tStr = (com.example.systemf.ast.Str) t;

			return new Str(tStr.getStr());
		}
		else if (t instanceof com.example.systemf.ast.Bool) {
			com.example.systemf.ast.Bool tBool = (com.example.systemf.ast.Bool) t;

			return new Bool(tBool.isBool());
		}
		else if (t instanceof com.example.systemf.ast.Var) {
			com.example.systemf.ast.Var tVar = (com.example.systemf.ast.Var) t;

			return new Var(tVar.getVar());
		}
		else if (t instanceof com.example.systemf.ast.Lam) {
			com.example.systemf.ast.Lam tLam = (com.example.systemf.ast.Lam) t;
			Term term;

			if (tLam.getLoc() == Location.Client)
				term = compClient(tLam.getM());
			else
				term = compServer(tLam.getM());

			return new Lam(tLam.getLoc(), tLam.getX(), tLam.getIdTy(), term);
		}
		else if (t instanceof com.example.systemf.ast.Tylam) {
			com.example.systemf.ast.Tylam tTylam = (com.example.systemf.ast.Tylam) t;
			Term term = compServer(tTylam.getTerm());

			return new Tylam(tTylam.getTy(), term);
		}
		else if (t instanceof com.example.systemf.ast.App) {
			com.example.systemf.ast.App tApp = (com.example.systemf.ast.App) t;

			String fvar = "f" + i++;
			String xvar = "x" + i++;
			String rvar = "r" + i++;

			Var f = new Var(fvar);
			Var x = new Var(xvar);
			Var r = new Var(rvar);

			Term fun = compServer(tApp.getFun());
			Term arg = compServer(tApp.getArg());

			ArrayList<Value> ws = new ArrayList<>();
			ws.add(x);

			if (tApp.getLoc() instanceof LocType) {
				Location loc = ((LocType) tApp.getLoc()).getLoc();

				if (loc == Location.Client) {
					String yvar = "y" + i++;
					String zvar = "z" + i++;

					Var y = new Var(yvar);
					Var z = new Var(zvar);

					ArrayList<Value> vs = new ArrayList<>();
					vs.add(z);

					Term commuteFun = new Lam(Location.Client, zvar, new Let(yvar, new App(f, vs), new Ret(y)));

					return new Let(fvar, fun, new Let(xvar, arg, new Let(rvar, new Call(commuteFun, ws), r)));
				}
				else
					return new Let(fvar, fun, new Let(xvar, arg, new Let(rvar, new App(f, ws), r)));
			}
			else
				throw new CompException("CompStaRpc(compClient) TypedLocation is not LocType" + tApp.getLoc());
		}
		else if (t instanceof com.example.systemf.ast.Tapp) {
			com.example.systemf.ast.Tapp tTApp = (com.example.systemf.ast.Tapp) t;

			String fvar = "f" + i++;
			String rvar = "r" + i++;

			Var f = new Var(fvar);
			Var r = new Var(rvar);

			Term fun = compClient(tTApp.getFun());

			return new Let(fvar, fun, new Let(rvar, new Tapp(f, tTApp.getTy()), r));
		}
		else if (t instanceof com.example.systemf.ast.Let) {
			com.example.systemf.ast.Let tLet = (com.example.systemf.ast.Let) t;

			Term term1 = compClient(tLet.getT1());
			Term term2 = compClient(tLet.getT2());

			return new Let(tLet.getId(), tLet.getIdTy(), term1, term2);
		}
		else if (t instanceof com.example.systemf.ast.If) {
			com.example.systemf.ast.If tIf = (com.example.systemf.ast.If) t;

			Term condTerm = compServer(tIf.getCond());
			Term thenTerm = compServer(tIf.getThenT());
			Term elseTerm = compServer(tIf.getElseT());

			String condvar = "c" + i++;
			String thenvar = "t" + i++;
			String elsevar = "e" + i++;
			String rvar = "r" + i++;

			Var condv = new Var(condvar);
			Var thenv = new Var(thenvar);
			Var elsev = new Var(elsevar);
			Var r = new Var(rvar);

			return new Let(condvar, condTerm, new Let(thenvar, thenTerm,
					new Let(elsevar, elseTerm, new Let(rvar, new If(condv, thenv, elsev), r))));
		}
		else if (t instanceof com.example.systemf.ast.ExprTerm) {
			com.example.systemf.ast.ExprTerm tExpr = (com.example.systemf.ast.ExprTerm) t;

			ArrayList<Term> oprnds = new ArrayList<>();
			ArrayList<Pair<String, Term>> pairOprnds = new ArrayList<>();

			// pairOprnds { (x2, oprnd2), (x1, oprnd1) }
			for (com.example.systemf.ast.Term oprnd : tExpr.getOprnds()) {
				Term compOprnd = compServer(oprnd);

				String xvar = "x" + i++;
				Pair<String, Term> p = new Pair<>(xvar, compOprnd);
				pairOprnds.add(0, p);
			}

			// oprnds { x1, x2 }
			for (Pair<String, Term> p : pairOprnds) {
				oprnds.add(0, new Var(p.getKey()));
			}

			String rvar = "r" + i++;
			Var r = new Var(rvar);

			Term expr = new Let(rvar, new ExprTerm(oprnds, tExpr.getOp()), r);

			for (int i = 0; i < pairOprnds.size(); i++) {
				Pair<String, Term> p = pairOprnds.get(i);
				expr = new Let(p.getKey(), p.getValue(), expr);
			}

			return expr;
		}
		else
			throw new CompException("CompStaRpc(compServer) not match " + t.getClass() + "(" + t + ")");
	}
}
