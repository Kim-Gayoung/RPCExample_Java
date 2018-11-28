package com.example.extrpc;

public class ExprTerm extends Cond {
	private Term oprnd1;
	private String op;
	private Term oprnd2;

	public ExprTerm(Term oprnd1, String op, Term oprnd2) {
		this.oprnd1 = oprnd1;
		this.op = op;
		this.oprnd2 = oprnd2;
	}

	public ExprTerm(Term oprnd1, String op) {
		this.oprnd1 = oprnd1;
		this.op = op;
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
		String ret = "";

		if (oprnd2 == null) {
			ret += op + oprnd1;
		} else {
			ret += oprnd1 + " " + op + " " + oprnd2;
		}

		return ret;
	}
	
	public static final String ADD = "+";
	public static final String SUB = "-";
	public static final String MUL = "*";
	public static final String DIV = "/";
	public static final String UNARY = "-";
	
	public static final String GTHAN = ">";
	public static final String GEQUAL = ">=";
	public static final String LTHAN = "<";
	public static final String LEQUAL = "<=";
	public static final String EQUAL = "==";
	public static final String NOTEQUAL = "!=";
	
	public static final String AND = "and";
	public static final String OR = "or";
	public static final String NOT = "!";
}
