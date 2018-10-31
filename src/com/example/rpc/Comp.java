package com.example.rpc;

public class Comp extends Cond {
	public static final String GTHAN = ">";
	public static final String GEQUAL = ">=";
	public static final String LTHAN = "<";
	public static final String LEQUAL = "<=";
	public static final String EQUAL = "==";
	public static final String NOTEQUAL = "!=";
	
	private Term oprnd1;
	private String op;
	private Term oprnd2;
	
	public Comp(Term oprnd1, String op, Term oprnd2) {
		super();
		this.oprnd1 = oprnd1;
		this.op = op;
		this.oprnd2 = oprnd2;
	}

	public Term getOprnd1() {
		return oprnd1;
	}

	public void setOprnd1(Term oprnd1) {
		this.oprnd1 = oprnd1;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public Term getOprnd2() {
		return oprnd2;
	}

	public void setOprnd2(Term oprnd2) {
		this.oprnd2 = oprnd2;
	}

	@Override
	public String toString() {
		String ret = oprnd1 + " " + op + " " + oprnd2;
		
		return ret;
	}

}
