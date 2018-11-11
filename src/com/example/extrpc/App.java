package com.example.extrpc;

public class App extends Term {
	private Term fun;
	private Term arg;
	private TypedLocation loc;

	public App(Term fun, Term arg) {
		this.fun = fun;
		this.arg = arg;
	}

	public Term getFun() {
		return fun;
	}

	public void setFun(Term fun) {
		this.fun = fun;
	}

	public Term getArg() {
		return arg;
	}

	public void setArg(Term arg) {
		this.arg = arg;
	}

	public TypedLocation getLoc() {
		return loc;
	}

	public void setLoc(TypedLocation loc) {
		this.loc = loc;
	}

	@Override
	public String toString() {
		String ret = "(" + fun.toString() + ")";
		
		if (loc != null)
			 ret += loc + "(" + arg.toString() + ")";
		else
			ret +=  " (" + arg.toString() + ")";
		
		return ret;
	}
}
