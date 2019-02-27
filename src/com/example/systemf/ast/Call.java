package com.example.systemf.ast;

public class Call extends Term {
	private Term fun;
	private Value ws;

	public Call(Term fun, Value ws) {
		this.fun = fun;
		this.ws = ws;
	}

	public Term getFun() {
		return fun;
	}

	public void setFun(Term fun) {
		this.fun = fun;
	}

	public Value getWs() {
		return ws;
	}

	public void setWs(Value ws) {
		this.ws = ws;
	}

	@Override
	public String toString() {
		return "Call(" + fun + ") (" + ws + ")";
	}	
}
