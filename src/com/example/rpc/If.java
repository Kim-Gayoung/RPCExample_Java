package com.example.rpc;

public class If extends Term {
	private Cond cond;
	private Term thenT;
	private Term elseT;
	
	public If(Cond cond, Term thenT, Term elseT) {
		this.cond = cond;
		this.thenT = thenT;
		this.elseT = elseT;
	}

	public Cond getCond() {
		return cond;
	}

	public void setCond(Cond cond) {
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
