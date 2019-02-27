package com.example.systemf;

import com.example.systemf.ast.App;
import com.example.systemf.ast.Bool;
import com.example.systemf.ast.Call;
import com.example.systemf.ast.ExprTerm;
import com.example.systemf.ast.If;
import com.example.systemf.ast.Lam;
import com.example.systemf.ast.Let;
import com.example.systemf.ast.LocType;
import com.example.systemf.ast.Location;
import com.example.systemf.ast.Num;
import com.example.systemf.ast.Req;
import com.example.systemf.ast.Ret;
import com.example.systemf.ast.Str;
import com.example.systemf.ast.TApp;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.TopLevel;
import com.example.systemf.ast.Tylam;
import com.example.systemf.ast.Unit;
import com.example.systemf.ast.Var;

public class CompStaRpc {
	private static int i = 1;
	
	public static TopLevel compStaRpc(TopLevel top) throws CompException {
		return compTopLevel(top);
	}
	
	public static TopLevel compTopLevel(TopLevel top) throws CompException {
		Term term = compClient(top.getTop());
		
		if (top.getNext() != null) {
			TopLevel next = compTopLevel(top.getNext());
			
			return new TopLevel(term, next);
		}
		else {
			return new TopLevel(term);
		}
	}
	
	public static Term compClient(Term t) throws CompException {
		if (t instanceof Unit)
			return t;
		else if (t instanceof Num)
			return t;
		else if (t instanceof Str)
			return t;
		else if (t instanceof Bool)
			return t;
		else if (t instanceof Var)
			return t;
		else if (t instanceof Lam) {
			Lam tLam = (Lam) t;
			Term term;
			
			if (tLam.getLoc() == Location.Client)
				term = compClient(tLam.getM());
			else
				term = compServer(tLam.getM());
			
			return new Lam(tLam.getLoc(), tLam.getX(), tLam.getIdTy(), term);
		}
		else if (t instanceof Tylam) {
			Tylam tTylam = (Tylam) t;
			Term term = compClient(tTylam.getTerm());
			
			return new Tylam(tTylam.getTy(), term);
		}
		else if (t instanceof App) {
			App tApp = (App) t;
			
			String fvar = "f" + i++;
			String xvar = "x" + i++;
			String rvar = "r" + i++;
			
			Var f = new Var(fvar);
			Var x = new Var(xvar);
			Var r = new Var(rvar);
			
			Term fun = compClient(tApp.getFun());
			Term arg = compClient(tApp.getArg());
			
			if (tApp.getLoc() instanceof LocType) {
				Location loc = ((LocType) tApp.getLoc()).getLoc();
				
				if (loc == Location.Client)
					return new Let(fvar, fun, new Let(xvar, arg, new Let(rvar, new App(f, x), r)));
				else
					return new Let(fvar, fun, new Let(xvar, arg, new Let(rvar, new Req(f, x), r)));
			}
			else
				throw new CompException("CompStaRpc(compClient) TypedLocation is not LocType" + tApp.getLoc());
		}
		else if (t instanceof TApp) {
			TApp tTApp = (TApp) t;
			
			String fvar = "f" + i++;
			String rvar = "r" + i++;
			
			Var f = new Var(fvar);
			Var r = new Var(rvar);
			
			Term fun = compClient(tTApp.getFun());
			
			return new Let(fvar, fun, new Let(rvar, new TApp(f, tTApp.getTy()), r));
		}
		else if (t instanceof Let) {
			Let tLet = (Let) t;
			
			Term term1 = compClient(tLet.getT1());
			Term term2 = compClient(tLet.getT2());
			
			return new Let(tLet.getId(), tLet.getIdTy(), term1, term2);
		}
		else if (t instanceof If) {
			If tIf = (If) t;
			
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
			
			return new Let(condvar, condTerm,
					new Let(thenvar, thenTerm,
							new Let(elsevar, elseTerm,
									new Let(rvar, new If(condv, thenv, elsev), r))));
		}
		else if (t instanceof ExprTerm) {
			ExprTerm tExpr = (ExprTerm) t;
			int op = tExpr.getOp();
			
			if (op == 4 || op == 13) {	// Unary Minus, Not Operation
				String xvar = "x" + i++;
				String rvar = "r" + i++;
				
				Var x = new Var(xvar);
				Var r = new Var(rvar);
				
				Term oprnd1 = compClient(tExpr.getOprnd1());
				
				return new Let(xvar, oprnd1, new Let(rvar, new ExprTerm(x, op), r));
			}
			else {
				String xvar = "x" + i++;
				String yvar = "y" + i++;
				String rvar = "r" + i++;
				
				Var x = new Var(xvar);
				Var y = new Var(yvar);
				Var r = new Var(rvar);
				
				Term oprnd1 = compClient(tExpr.getOprnd1());
				Term oprnd2 = compClient(tExpr.getOprnd2());
				
				return new Let(xvar, oprnd1,
						new Let(yvar, oprnd2,
								new Let(rvar, new ExprTerm(x, op, y), r)));
			}
		}
		else
			throw new CompException("CompStaRpc(compClient) not match " + t.getClass() + "(" + t + ")");
	}
	
	public static Term compServer(Term t) throws CompException {
		if (t instanceof Unit)
			return t;
		else if (t instanceof Num)
			return t;
		else if (t instanceof Str)
			return t;
		else if (t instanceof Bool)
			return t;
		else if (t instanceof Var)
			return t;
		else if (t instanceof Lam) {
			Lam tLam = (Lam) t;
			Term term;
			
			if (tLam.getLoc() == Location.Client)
				term = compClient(tLam.getM());
			else
				term = compServer(tLam.getM());
			
			return new Lam(tLam.getLoc(), tLam.getX(), tLam.getIdTy(), term);
		}
		else if (t instanceof Tylam) {
			Tylam tTylam = (Tylam) t;
			Term term = compServer(tTylam.getTerm());
			
			return new Tylam(tTylam.getTy(), term);
		}
		else if (t instanceof App) {
			App tApp = (App) t;
			
			String fvar = "f" + i++;
			String xvar = "x" + i++;
			String rvar = "r" + i++;
			
			Var f = new Var(fvar);
			Var x = new Var(xvar);
			Var r = new Var(rvar);
			
			Term fun = compServer(tApp.getFun());
			Term arg = compServer(tApp.getArg());
			
			if (tApp.getLoc() instanceof LocType) {
				Location loc = ((LocType) tApp.getLoc()).getLoc();
				
				if (loc == Location.Client) {
					String yvar = "y" + i++;
					String zvar = "z" + i++;
					
					Var y = new Var(yvar);
					Var z = new Var(zvar);
					
					Term commuteFun = new Lam(Location.Client, zvar, new Let(yvar, new App(f, z), new Ret(y)));
					
					return new Let(fvar, fun,
							new Let(xvar, arg,
									new Let(rvar, new Call(commuteFun, x), r)));
				}
				else
					return new Let(fvar, fun, new Let(xvar, arg, new Let(rvar, new App(f, x), r)));
			}
			else
				throw new CompException("CompStaRpc(compClient) TypedLocation is not LocType" + tApp.getLoc());
		}
		else if (t instanceof TApp) {
			TApp tTApp = (TApp) t;
			
			String fvar = "f" + i++;
			String rvar = "r" + i++;
			
			Var f = new Var(fvar);
			Var r = new Var(rvar);
			
			Term fun = compClient(tTApp.getFun());
			
			return new Let(fvar, fun, new Let(rvar, new TApp(f, tTApp.getTy()), r));
		}
		else if (t instanceof Let) {
			Let tLet = (Let) t;
			
			Term term1 = compClient(tLet.getT1());
			Term term2 = compClient(tLet.getT2());
			
			return new Let(tLet.getId(), tLet.getIdTy(), term1, term2);
		}
		else if (t instanceof If) {
			If tIf = (If) t;
			
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
			
			return new Let(condvar, condTerm,
					new Let(thenvar, thenTerm,
							new Let(elsevar, elseTerm,
									new Let(rvar, new If(condv, thenv, elsev), r))));
		}
		else if (t instanceof ExprTerm) {
			ExprTerm tExpr = (ExprTerm) t;
			int op = tExpr.getOp();
			
			if (op == 4 || op == 13) {	// Unary Minus, Not Operation
				String xvar = "x" + i++;
				String rvar = "r" + i++;
				
				Var x = new Var(xvar);
				Var r = new Var(rvar);
				
				Term oprnd1 = compServer(tExpr.getOprnd1());
				
				return new Let(xvar, oprnd1, new Let(rvar, new ExprTerm(x, op), r));
			}
			else {
				String xvar = "x" + i++;
				String yvar = "y" + i++;
				String rvar = "r" + i++;
				
				Var x = new Var(xvar);
				Var y = new Var(yvar);
				Var r = new Var(rvar);
				
				Term oprnd1 = compServer(tExpr.getOprnd1());
				Term oprnd2 = compServer(tExpr.getOprnd2());
				
				return new Let(xvar, oprnd1,
						new Let(yvar, oprnd2,
								new Let(rvar, new ExprTerm(x, op, y), r)));
			}
		}
		else
			throw new CompException("CompStaRpc(compServer) not match " + t.getClass() + "(" + t + ")");
	}
}
