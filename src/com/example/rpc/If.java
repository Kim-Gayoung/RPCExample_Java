package com.example.rpc;

public class If extends Term {
	private Term cond;
	private Term thenT;
	private Term elseT;
	
	public If(Term cond, Term thenT, Term elseT) {
		this.cond = cond;
		this.thenT = thenT;
		this.elseT = elseT;
	}

	public Term getCond() {
		return cond;
	}

	public void setCond(Term cond) {
		this.cond = cond;
	}

	public Term getThenT() {
		return thenT;
	}

	public void setThenT(Term thenT) {
		this.thenT = thenT;
	}

	public Term getElseT() {
		return elseT;
	}

	public void setElseT(Term elseT) {
		this.elseT = elseT;
	}

	@Override
	public String toString() {
		String ret = "if " + cond + " then\n\t" + thenT + "\n";
		ret += "else\n\t" + elseT + "\n";
		
		return ret;		
	}
	
}
