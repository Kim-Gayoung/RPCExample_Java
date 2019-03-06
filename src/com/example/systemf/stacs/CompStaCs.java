package com.example.systemf.stacs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.example.systemf.CompException;
import com.example.systemf.ast.Location;
import com.example.systemf.ast.Type;
import com.example.systemf.sta.ast.App;
import com.example.systemf.sta.ast.Bool;
import com.example.systemf.sta.ast.Call;
import com.example.systemf.sta.ast.Clo;
import com.example.systemf.sta.ast.PrimTerm;
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
import com.example.utils.TripleTup;

public class CompStaCs {
	private static int i = 1;

	public static TripleTup<Term, FunStore, FunStore> cloConvTerm(Term term) throws CompException {
		return cloConv(term, new ArrayList<>());
	}

	public static TripleTup<Term, FunStore, FunStore> cloConv(Term t, ArrayList<String> zs) throws CompException {
		TripleTup<Term, FunStore, FunStore> ret;

		if (t instanceof Unit) {
			Unit tUnit = (Unit) t;

			return new TripleTup<>(tUnit, new FunStore(), new FunStore());
		}
		else if (t instanceof Num) {
			Num tNum = (Num) t;

			return new TripleTup<>(tNum, new FunStore(), new FunStore());
		}
		else if (t instanceof Str) {
			Str tStr = (Str) t;

			return new TripleTup<>(tStr, new FunStore(), new FunStore());
		}
		else if (t instanceof Bool) {
			Bool tBool = (Bool) t;

			return new TripleTup<>(tBool, new FunStore(), new FunStore());
		}
		else if (t instanceof Var) {
			Var tVar = (Var) t;

			return new TripleTup<>(tVar, new FunStore(), new FunStore());
		}
		else if (t instanceof Lam) {
			Lam tLam = (Lam) t;

			ArrayList<String> strs = new ArrayList<>();
			strs.addAll(zs);
			strs.add(tLam.getX());

			TripleTup<Term, FunStore, FunStore> p1 = cloConv(tLam.getM(), strs);

			ArrayList<String> fvs = fv(tLam);
			ArrayList<Type> ftvs = ftv(tLam);

			ArrayList<String> lamVars = new ArrayList<>();
			lamVars.add(tLam.getX());

			ClosedFun closedFun = new ClosedFun(ftvs, fvs, tLam.getLoc(), lamVars, p1.getFirst());

			String f = "_gf" + i++;

			FunStore client = p1.getSecond();
			FunStore server = p1.getThird();

			if (tLam.getLoc() == Location.Client)
				client.getFs().put(f, closedFun);
			else
				server.getFs().put(f, closedFun);

			ArrayList<Value> cloVs = new ArrayList<>();

			for (String z : fvs) {
				cloVs.add(new Var(z));
			}

			ret = new TripleTup<>(new Clo(f, ftvs, cloVs), client, server);

			return ret;
		}
		else if (t instanceof Tylam) {
			Tylam tTylam = (Tylam) t;

			String f = "_f" + i++;

			TripleTup<Term, FunStore, FunStore> p1 = cloConv(tTylam.getTerm(), zs);

			ArrayList<String> fvs = fv(tTylam);
			ArrayList<Type> ftvs = ftv(tTylam);

			ClosedFun closedFunServer = new ClosedFun(ftvs, fvs, Location.Server, new ArrayList<String>(),
					p1.getFirst());
			ClosedFun closedFunClient = new ClosedFun(ftvs, fvs, Location.Client, new ArrayList<String>(),
					p1.getFirst());

			FunStore client = p1.getSecond();
			client.getFs().put(f, closedFunClient);
			FunStore server = p1.getThird();
			server.getFs().put(f, closedFunServer);

			ArrayList<Value> cloVs = new ArrayList<>();

			for (String z : fvs) {
				cloVs.add(new Var(z));
			}
			ret = new TripleTup<>(new Clo(f, ftvs, cloVs), client, server);

			return ret;
		}
		else if (t instanceof App) {
			App tApp = (App) t;

			TripleTup<Term, FunStore, FunStore> p1 = cloConv(tApp.getFun(), zs);
			TripleTup<ArrayList<Value>, FunStore, FunStore> p2 = cloConvList(0, tApp.getWs(), zs);

			FunStore client = p1.getSecond();
			client.getFs().putAll(p2.getSecond().getFs());

			FunStore server = p1.getThird();
			server.getFs().putAll(p2.getThird().getFs());

			ret = new TripleTup<>(new App(p1.getFirst(), p2.getFirst()), client, server);

			return ret;
		}
		else if (t instanceof Tapp) {
			Tapp tTApp = (Tapp) t;

			TripleTup<Term, FunStore, FunStore> p1 = cloConv(tTApp.getFun(), zs);

			ret = new TripleTup<>(new Tapp(p1.getFirst(), tTApp.getTy()), p1.getSecond(), p1.getThird());
			
			return ret;
		}
		else if (t instanceof Let) {
			Let tLet = (Let) t;
			
			TripleTup<Term, FunStore, FunStore> p1 = cloConv(tLet.getT1(), zs);
			
			ArrayList<String> strArr = (ArrayList<String>) zs.clone();
			strArr.add(tLet.getId());
			
			TripleTup<Term, FunStore, FunStore> p2 = cloConv(tLet.getT2(), strArr);
			
			FunStore client = p1.getSecond();
			client.getFs().putAll(p2.getSecond().getFs());
			
			FunStore server = p1.getThird();
			server.getFs().putAll(p2.getThird().getFs());
			
			ret = new TripleTup<>(new Let(tLet.getId(), tLet.getIdTy(), p1.getFirst(), p2.getFirst()), client, server);
			
			return ret;
		}
		else if (t instanceof Call) {
			Call tCall = (Call) t;

			TripleTup<Term, FunStore, FunStore> p1 = cloConv(tCall.getFun(), zs);
			TripleTup<ArrayList<Value>, FunStore, FunStore> p2 = cloConvList(0, tCall.getWs(), zs);

			FunStore client = p1.getSecond();
			client.getFs().putAll(p2.getSecond().getFs());

			FunStore server = p1.getThird();
			server.getFs().putAll(p2.getThird().getFs());

			ret = new TripleTup<>(new App(p1.getFirst(), p2.getFirst()), client, server);

			return ret;
		}
		else if (t instanceof Req) {
			Req tReq = (Req) t;

			TripleTup<Term, FunStore, FunStore> p1 = cloConv(tReq.getFun(), zs);
			TripleTup<ArrayList<Value>, FunStore, FunStore> p2 = cloConvList(0, tReq.getWs(), zs);

			FunStore client = p1.getSecond();
			client.getFs().putAll(p2.getSecond().getFs());

			FunStore server = p1.getThird();
			server.getFs().putAll(p2.getThird().getFs());

			ret = new TripleTup<>(new App(p1.getFirst(), p2.getFirst()), client, server);

			return ret;
		}
		else if (t instanceof Ret) {
			Ret tRet = (Ret) t;

			TripleTup<Term, FunStore, FunStore> p1 = cloConv(tRet.getW(), zs);

			ret = new TripleTup<>(new Ret((Value) p1.getFirst()), p1.getSecond(), p1.getThird());

			return ret;
		}
		else if (t instanceof If) {
			If tIf = (If) t;

			TripleTup<Term, FunStore, FunStore> condClo = cloConv(tIf.getCond(), zs);
			TripleTup<Term, FunStore, FunStore> thenClo = cloConv(tIf.getThenT(), zs);
			TripleTup<Term, FunStore, FunStore> elseClo = cloConv(tIf.getElseT(), zs);

			FunStore client = condClo.getSecond();
			client.getFs().putAll(thenClo.getSecond().getFs());
			client.getFs().putAll(elseClo.getSecond().getFs());

			FunStore server = condClo.getThird();
			server.getFs().putAll(thenClo.getThird().getFs());
			server.getFs().putAll(elseClo.getThird().getFs());

			ret = new TripleTup<>(new If(condClo.getFirst(), thenClo.getFirst(), elseClo.getFirst()), client, server);

			return ret;
		}
		else if (t instanceof PrimTerm) {
			PrimTerm tExpr = (PrimTerm) t;
			ArrayList<Term> oprnds = new ArrayList<>();
			FunStore client = new FunStore();
			FunStore server = new FunStore();

			for (Term oprnd: tExpr.getOprnds()) {
				TripleTup<Term, FunStore, FunStore> p = cloConv(oprnd, zs);
				oprnds.add(p.getFirst());
				
				client.getFs().putAll(p.getSecond().getFs());
				server.getFs().putAll(p.getThird().getFs());
			}
			
			ret = new TripleTup<>(new PrimTerm(oprnds, tExpr.getOp()), client, server);
			
			return ret;
		}
		else
			throw new CompException("Not Expected Term: " + t);
	}

	public static TripleTup<ArrayList<Value>, FunStore, FunStore> cloConvList(int idx, ArrayList<Value> ms,
			ArrayList<String> zs) throws CompException {
		TripleTup<ArrayList<Value>, FunStore, FunStore> ret;

		if (idx == ms.size()) {
			ret = new TripleTup<>(new ArrayList<Value>(), new FunStore(), new FunStore());

			return ret;
		}
		else {
			Term m = ms.get(idx);

			TripleTup<ArrayList<Value>, FunStore, FunStore> p1 = cloConvList(idx + 1, ms, zs);
			TripleTup<Term, FunStore, FunStore> p2 = cloConv(m, zs);

			ArrayList<Value> valList = p1.getFirst();
			valList.add((Value) p2.getFirst());

			FunStore client = p2.getSecond();
			client.getFs().putAll(p1.getSecond().getFs());

			FunStore server = p2.getThird();
			server.getFs().putAll(p1.getThird().getFs());

			ret = new TripleTup<>(valList, client, server);

			return ret;
		}
	}

	public static ArrayList<String> fv(Term m) {
		ArrayList<String> retList = new ArrayList<>();
		Set<String> strSet = new HashSet<>();

		if (m instanceof Unit) {
			return retList;
		}
		else if (m instanceof Num) {
			return retList;
		}
		else if (m instanceof Str) {
			return retList;
		}
		else if (m instanceof Bool) {
			return retList;
		}
		else if (m instanceof Var) {
			Var mVar = (Var) m;

			retList.add(mVar.getVar());

			return retList;
		}
		else if (m instanceof Lam) {
			Lam mLam = (Lam) m;

			strSet.addAll(fv(mLam.getM()));
			strSet.remove(mLam.getX());

			retList.addAll(strSet);

			return retList;
		}
		else if (m instanceof Tylam) {
			Tylam mTylam = (Tylam) m;
			// tylam id. term

			return fv(mTylam.getTerm());
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;

			strSet.addAll(fv(mCall.getFun()));
			for (Value v : mCall.getWs()) {
				strSet.addAll(fv(v));
			}
			retList.addAll(strSet);

			return retList;
		}
		else if (m instanceof Ret) {
			Ret mRet = (Ret) m;

			return fv(mRet.getW());
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;

			strSet.addAll(fv(mReq.getFun()));
			for (Value v : mReq.getWs()) {
				strSet.addAll(fv(v));
			}
			retList.addAll(strSet);

			return retList;
		}
		else if (m instanceof App) {
			App mApp = (App) m;

			strSet.addAll(fv(mApp.getFun()));
			for (Value v : mApp.getWs()) {
				strSet.addAll(fv(v));
			}
			retList.addAll(strSet);

			return retList;
		}
		else if (m instanceof Tapp) {
			Tapp mTApp = (Tapp) m;
			// fun type

			return fv(mTApp.getFun());
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;

			strSet.addAll(fv(mLet.getT2()));
			strSet.remove(mLet.getId());
			strSet.addAll(fv(mLet.getT1()));

			retList.addAll(strSet);

			return retList;
		}
		else if (m instanceof If) {
			If mIf = (If) m;

			strSet.addAll(fv(mIf.getThenT()));
			strSet.addAll(fv(mIf.getElseT()));

			retList.addAll(strSet);

			return retList;
		}
		else if (m instanceof PrimTerm) {
			PrimTerm mExpr = (PrimTerm) m;

			for (Term oprnd: mExpr.getOprnds()) {
				strSet.addAll(fv(oprnd));
			}

			retList.addAll(strSet);

			return retList;
		}
		else
			return null;
	}

	public static ArrayList<Type> ftv(Term m) {
		ArrayList<Type> retList = new ArrayList<>();
		Set<Type> tySet = new HashSet<>();

		if (m instanceof Unit) {
			return retList;
		}
		else if (m instanceof Num) {
			return retList;
		}
		else if (m instanceof Str) {
			return retList;
		}
		else if (m instanceof Bool) {
			return retList;
		}
		else if (m instanceof Var) {
			return retList;
		}
		else if (m instanceof Lam) {
			Lam mLam = (Lam) m;

			return ftv(mLam.getM());
		}
		else if (m instanceof Tylam) {
			Tylam mTylam = (Tylam) m;

			tySet.addAll(ftv(mTylam.getTerm()));
			tySet.remove(mTylam.getTy());

			retList.addAll(tySet);

			return retList;
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;

			tySet.addAll(ftv(mCall.getFun()));
			for (Value v : mCall.getWs()) {
				tySet.addAll(ftv(v));
			}

			retList.addAll(tySet);

			return retList;
		}
		else if (m instanceof Ret) {
			Ret mRet = (Ret) m;

			return ftv(mRet.getW());
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;

			tySet.addAll(ftv(mReq.getFun()));
			for (Value v : mReq.getWs()) {
				tySet.addAll(ftv(v));
			}

			retList.addAll(tySet);

			return retList;
		}
		else if (m instanceof App) {
			App mApp = (App) m;

			tySet.addAll(ftv(mApp.getFun()));
			for (Value v : mApp.getWs()) {
				tySet.addAll(ftv(v));
			}

			retList.addAll(tySet);

			return retList;
		}
		else if (m instanceof Tapp) {
			Tapp mTApp = (Tapp) m;

			tySet.addAll(ftv(mTApp.getFun()));
			tySet.remove(mTApp.getTy());

			retList.addAll(tySet);

			return retList;
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;

			tySet.addAll(ftv(mLet.getT2()));
			tySet.addAll(ftv(mLet.getT1()));

			retList.addAll(tySet);

			return retList;
		}
		else if (m instanceof If) {
			If mIf = (If) m;

			tySet.addAll(ftv(mIf.getThenT()));
			tySet.addAll(ftv(mIf.getElseT()));

			retList.addAll(tySet);

			return retList;
		}
		else if (m instanceof PrimTerm) {
			PrimTerm mExpr = (PrimTerm) m;

			for (Term oprnd: mExpr.getOprnds()) {
				tySet.addAll(ftv(oprnd));
			}

			retList.addAll(tySet);

			return retList;
		}
		else
			return null;
	}
}
