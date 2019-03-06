package com.example.systemf.stacs;

import java.util.ArrayList;

import com.example.systemf.sta.ast.App;
import com.example.systemf.sta.ast.Bool;
import com.example.systemf.sta.ast.Call;
import com.example.systemf.sta.ast.Clo;
import com.example.systemf.sta.ast.ExprTerm;
import com.example.systemf.sta.ast.Let;
import com.example.systemf.sta.ast.Num;
import com.example.systemf.sta.ast.Req;
import com.example.systemf.sta.ast.Ret;
import com.example.systemf.sta.ast.Str;
import com.example.systemf.sta.ast.Term;
import com.example.systemf.sta.ast.Unit;
import com.example.systemf.sta.ast.Value;
import com.example.systemf.sta.ast.Var;

import javafx.util.Pair;

public class SubstStaCS {
	public static Term subst(Term m, String x, Value v) {
		if (m instanceof Unit)
			return m;
		else if (m instanceof Num)
			return m;
		else if (m instanceof Str)
			return m;
		else if (m instanceof Bool)
			return m;
		else if (m instanceof Var) {
			Var mVar = (Var) m;

			if (mVar.getVar().equals(x))
				return v;
			else
				return mVar;
		}
		else if (m instanceof Clo) {
			Clo mClo = (Clo) m;

			ArrayList<Value> vs = new ArrayList<>();

			for (Value value : mClo.getVs()) {
				vs.add((Value) subst(value, x, v));
			}

			return new Clo(mClo.getF(), mClo.getTs(), vs);
		}
		else if (m instanceof App) {
			App mApp = (App) m;

			Value f = (Value) subst(mApp.getFun(), x, v);

			ArrayList<Value> ws = new ArrayList<>();

			for (Value w : mApp.getWs()) {
				ws.add((Value) subst(w, x, v));
			}

			return new App(f, ws);
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;

			Value f = (Value) subst(mCall.getFun(), x, v);

			ArrayList<Value> ws = new ArrayList<>();

			for (Value w : mCall.getWs()) {
				ws.add((Value) subst(w, x, v));
			}

			return new Call(f, ws);
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;

			Value f = (Value) subst(mReq.getFun(), x, v);

			ArrayList<Value> ws = new ArrayList<>();

			for (Value w : mReq.getWs()) {
				ws.add((Value) subst(w, x, v));
			}

			return new Req(f, ws);
		}
		else if (m instanceof Ret) {
			Ret mRet = (Ret) m;
			
			Value w = (Value) subst(mRet.getW(), x, v);
			
			return new Ret(w);
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;
			
			Term t1 = subst(mLet.getT1(), x, v);
			Term t2;
			
			if (x.equals(mLet.getId()))
				t2 = mLet.getT2();
			else
				t2 = subst(mLet.getT2(), x, v);
			
			return new Let(mLet.getId(), mLet.getIdTy(), t1, t2);
		}
		else if (m instanceof ExprTerm) {
			ExprTerm mExpr = (ExprTerm) m;
			
			ArrayList<Term> oprnds = new ArrayList<>();
			
			for (Term t: mExpr.getOprnds())
				oprnds.add(subst(t, x, v));
			
			return new ExprTerm(oprnds, mExpr.getOp());
		}
		else {
			System.out.println("Term(" + m.getClass() + ") is not expected.");
			return null;
		}
	}
	
	public static Term substs(Term m, ArrayList<String> xs, ArrayList<Value> vs) {
		ArrayList<Pair<String, Value>> pairList = new ArrayList<>();
		
		for (int i = 0; i < xs.size(); i++)
			pairList.add(new Pair<>(xs.get(i), vs.get(i)));
		
		Term t = m;
		
		for (Pair<String, Value> p: pairList) {
			String x = p.getKey();
			Value v = p.getValue();
			
			t = subst(t, x, v);
		}
		
		return t;
	}
}
