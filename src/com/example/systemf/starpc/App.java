package com.example.systemf.starpc;

import java.util.ArrayList;

public class App extends Term {
	private Term fun;
	private ArrayList<Value> ws;

	public App(Term fun, ArrayList<Value> ws) {
		super();
		this.fun = fun;
		this.ws = ws;
	}

	public Term getFun() {
		return fun;
	}

	public void setFun(Term fun) {
		this.fun = fun;
	}

	public ArrayList<Value> getWs() {
		return ws;
	}

	public void setWs(ArrayList<Value> ws) {
		this.ws = ws;
	}

	@Override
	public String toString() {
		int cnt = 0;
		String ret = "(" + fun.toString() + ") (";

		for (Value sv : ws) {
			ret += sv.toString();

			if (ws.size() - 1 != cnt) {
				ret += " ";
				cnt++;
			}
		}

		ret += ")";

		return ret;
	}

}
