package com.example.rpc;

public class IfStmt {
	private Condition cond;
	private Term thenTerm;
	private Term elseTerm;
	
	public IfStmt(Condition cond, Term thenTerm, Term elseTerm) {
		this.cond = cond;
		this.thenTerm = thenTerm;
		this.elseTerm = elseTerm;
	}
	
	public IfStmt(Condition cond, Term thenTerm) {
		this.cond = cond;
		this.thenTerm = thenTerm;
	}

	public Condition getCond() {
		return cond;
	}

	public void setCond(Condition cond) {
		this.cond = cond;
	}

	public Term getThenTerm() {
		return thenTerm;
	}

	public void setThenTerm(Term thenTerm) {
		this.thenTerm = thenTerm;
	}

	public Term getElseTerm() {
		return elseTerm;
	}

	public void setElseTerm(Term elseTerm) {
		this.elseTerm = elseTerm;
	}
	
	@Override
	public String toString() {
		String ret = "if " + cond + " then " + thenTerm;
		
		if (elseTerm != null)
			ret += " else " + elseTerm;
		
		return ret;
	}
}
