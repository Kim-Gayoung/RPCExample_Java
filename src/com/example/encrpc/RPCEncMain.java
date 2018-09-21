package com.example.encrpc;

import java.util.ArrayList;

import javafx.util.Pair;

public class RPCEncMain {
	public static EncTerm subst(EncTerm m, String x, EncValue v) {
		if (m instanceof Const) {
			Const mConst = (Const) m;

			return mConst;
		}
		else if (m instanceof Var) {
			Var mVar = (Var) m;

			if (mVar.getX().equals(x))
				return v;
			else
				return mVar;
		}
		else if (m instanceof Lam) {
			Lam mLam = (Lam) m;
			
			if (mLam.getStrArr().contains(x)) {
				return new Lam(mLam.getLoc(), mLam.getStrArr(), mLam.getTerm());
			}
			else {
				return new Lam(mLam.getLoc(), mLam.getStrArr(), subst(mLam.getTerm(), x, v));
			}
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;

			EncTerm call = subst(mCall.getCall(), x, v);
			ArrayList<EncValue> args = new ArrayList<>();

			for (EncValue ev : mCall.getArgs()) {
				EncTerm arg = subst(ev, x, v);
				if (arg instanceof EncValue)
					args.add((EncValue) arg);
			}

			Call retCall = new Call((EncValue) call, args);

			return retCall;
		}
		else if (m instanceof App) {
			App mApp = (App) m;

			EncTerm fun = subst(mApp.getFun(), x, v);
			ArrayList<EncValue> args = new ArrayList<>();

			for (EncValue ev : mApp.getArgs()) {
				EncTerm arg = subst(ev, x, v);

				if (arg instanceof EncValue)
					args.add((EncValue) arg);
			}
			App retApp = new App((EncValue) fun, args);

			return retApp;
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;

			EncTerm req = subst(mReq.getReq(), x, v);
			ArrayList<EncValue> args = new ArrayList<>();

			for (EncValue ev : mReq.getArgs()) {
				EncTerm arg = subst(ev, x, v);

				if (arg instanceof EncValue)
					args.add((EncValue) arg);
			}
			Req retReq = new Req((EncValue) req, args);

			return retReq;
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;

			EncTerm m1 = subst(mLet.getM1(), x, v);
			EncTerm m2;

			if (mLet.getVal().equals(x))
				m2 = mLet.getM2();
			else
				m2 = subst(mLet.getM2(), x, v);

			Let retLet = new Let(mLet.getVal(), m1, m2);

			return retLet;
		}
		return null;
	}

	// xs, vs != null
	public static EncTerm substs(EncTerm m, ArrayList<String> xs, ArrayList<EncValue> vs) {
		ArrayList<Pair<String, EncValue>> pairList = new ArrayList<>();
		for (int i = 0; i < xs.size(); i++) {
			pairList.add(new Pair<>(xs.get(i), vs.get(i)));
		}
		
		EncTerm encTerm = m;
		
		for (Pair<String, EncValue> p:pairList) {
			String x = p.getKey();
			EncValue v = p.getValue();

			encTerm = subst(encTerm, x, v);
		}
		
		return encTerm;
	}
	
	public static ArrayList<String> fv(EncTerm m) {
		ArrayList<String> retList = new ArrayList<>();
		
		if (m instanceof Const) {
			return retList;
		}
		else if (m instanceof Var) {
			Var mVar = (Var) m;
			
			retList.add(mVar.getX());
			
			return retList;
		}
		else if (m instanceof Lam) {
			Lam mLam = (Lam) m;
			
			// addAll 자체가 union을 위해 존재
			retList.addAll(fv(mLam.getTerm()));			// fv mbody
			retList.removeAll(mLam.getStrArr());		// (fv mbody) \\ xs
			
			return retList;
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;
			
			retList.addAll(fv(mCall.getCall()));
			
			for (EncValue v: mCall.getArgs()) {
				retList.addAll(fv(v));
			}
			
			return retList;
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;
			
			retList.addAll(fv(mReq.getReq()));
			
			for (EncValue v: mReq.getArgs()) {
				retList.addAll(fv(v));
			}
			
			return retList;
		}
		else if (m instanceof App) {
			App mApp = (App) m;
			
			retList.addAll(fv(mApp.getFun()));
			
			for (EncValue v: mApp.getArgs()) {
				retList.addAll(fv(v));
			}
			
			return retList;
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;
			
			retList.addAll(fv(mLet.getM2()));
			retList.remove(mLet.getVal());
			retList.addAll(fv(mLet.getM1()));
			
			return retList;
		}
		else
			return null;
	}
}