package com.example.extrpc;

import java.util.ArrayList;

public class CompStaRPCTerm {
	private static ArrayList<String> convList = new ArrayList<>();
	
	public static Term comp(TopLevel top) {
		TopLevel copyTop = copyFunc(top);
		TopLevel convTop = convertTop(copyTop);
		TopLevel compTop = compTop(convTop);
		
		return compTop;
	}
	
	public static TopLevel copyFunc(TopLevel top) {
		Term topTerm = top.getTerm();
		Term client = null;
		Term server = null;
		
		if (topTerm instanceof Let) {
			Let tLet = (Let) topTerm;
			Term letT1  = tLet.getT1();
			
			if (letT1 instanceof Lam) {
				Lam t1Lam = (Lam) letT1;
				
				if (t1Lam.getLoc() == Location.Polymorphic) {
					convList.add(tLet.getId());
					
					Term serverLam = new Lam(Location.Server, t1Lam.getX(), t1Lam.getM());
					server = new Let(tLet.getId()+"_server", tLet.getIdTy(), serverLam, tLet.getT2());
					
					Term clientLam = new Lam(Location.Client, t1Lam.getX(), t1Lam.getM());
					client = new Let(tLet.getId()+"_client", tLet.getIdTy(), clientLam, tLet.getT2());
				}
			}
		}
		
		if (top.getNext() != null) {
			TopLevel copyNext = copyFunc(top.getNext());
			
			if (server != null && client != null)
				return new TopLevel(server, new TopLevel(client, copyNext));
			else
				return new TopLevel(topTerm, copyNext);
		}
		else {
			if (server != null && client != null)
				return new TopLevel(server, new TopLevel(client));
			else
				return new TopLevel(topTerm);
		}
	}
	
	public static TopLevel convertTop(TopLevel top) {
		Term topTerm = convert(top.getTerm(), Location.Client);
		
		if (top.getNext() != null) {
			TopLevel nextTop = convertTop(top.getNext());
			
			return new TopLevel(topTerm, nextTop);
		}
		else {
			return new TopLevel(topTerm);
		}
	}
	
	public static Term convert(Term t, Location loc) {
		if (t instanceof Unit)
			return t;
		else if (t instanceof Num)
			return t;
		else if (t instanceof Str)
			return t;
		else if (t instanceof Bool)
			return t;
		else if (t instanceof Var) {
			Var tVar = (Var) t;
			
			if (convList.contains(tVar.getVar())) {
				if (loc == Location.Client)
					return new Var(tVar.getVar() + "_client");
				else
					return new Var(tVar.getVar() + "_server");
			}
			else
				return tVar;
		}
		else if (t instanceof Lam) {
			Lam tLam = (Lam) t;
			Location lamLoc = tLam.getLoc();
			
			Term body = convert(tLam.getM(), lamLoc);
			
			return new Lam(lamLoc, tLam.getX(), body);
		}
		else if (t instanceof App) {
			App tApp = (App) t;
			TypedLocation appLocType = tApp.getLoc();
			
			if (appLocType instanceof LocType) {
				Location appLoc = ((LocType) appLocType).getLoc();
				
				Term fun = convert(tApp.getFun(), appLoc);
				Term arg = convert(tApp.getArg(), appLoc);
				
				return new App(fun, arg, new LocType(appLoc));
			}
			else {
				LocVarType appLocVar = (LocVarType) appLocType;
				throw new RuntimeException("Location Type is not LocType. Check this LocVarType l" + appLocVar.getVar());
			}
			
		}
		else if (t instanceof Let) {
			Let tLet = (Let) t;
			
			Term t1 = convert(tLet.getT1(), loc);
			Term t2 = convert(tLet.getT2(), loc);
			
			return new Let(tLet.getId(), tLet.getIdTy(), t1, t2);
		}
		else if (t instanceof If) {
			If tIf = (If) t;
			
			Term condTerm = convert(tIf.getCond(), loc);
			Term thenTerm = convert(tIf.getThenT(), loc);
			Term elseTerm = convert(tIf.getElseT(), loc);
			
			return new If(condTerm, thenTerm, elseTerm);
		}
		else if (t instanceof ExprTerm) {
			ExprTerm tExpr = (ExprTerm) t;
			int op = tExpr.getOp();
			
			if (op == 4 || op == 13) {		// 4 = UNARY MINUS, 13 = NOT
				Term oprnd1 = convert(tExpr.getOprnd1(), loc);

				return new ExprTerm(oprnd1, op);
			}
			else {
				Term oprnd1 = convert(tExpr.getOprnd1(), loc);
				Term oprnd2 = convert(tExpr.getOprnd2(), loc);
				
				return new ExprTerm(oprnd1, op, oprnd2);
			}
		}
		else
			return null;
	}
	
	public static TopLevel compTop(TopLevel top) {
		return null;
	}
	
	public static Term compClient(Term t) {
		return null;
	}
	
	public static Term compServer(Term t) {
		return null;
	}
}
