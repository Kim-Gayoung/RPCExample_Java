package com.example.systemf.ast;

public class Req extends Term {
	private Term fun;
	private Value arg;
	
	public Req(Term fun, Value arg) {
		this.fun = fun;
		this.arg = arg;
	}

	public Term getFun() {
		return fun;
	}

	public void setFun(Term fun) {
		this.fun = fun;
	}

	public Value getArg() {
		return arg;
	}

	public void setArg(Value arg) {
		this.arg = arg;
	}

	@Override
	public String toString() {
		return "Req(" + fun + ") (" + arg + ")";
	}
	
}
