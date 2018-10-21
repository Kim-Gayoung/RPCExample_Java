package com.example.rpc;

public class Compare extends Condition {
	private Term oprnd1;
	private String op;
	private Term oprnd2;

	public Compare(Term oprnd1, String op, Term oprnd2) {
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

	public static final String GREATER_THAN = ">";
	public static final String GREATER_EQUAL = ">=";
	public static final String LESS_THAN= "<";
	public static final String LESS_EQUAL = "<=";
	
	public static final String EQUAL = "==";
	public static final String NOT_EQUAL = "!=";
}
