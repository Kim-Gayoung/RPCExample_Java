package com.example.extrpc;

public class ExprTerm extends Cond {
	private Term oprnd1;
	private int op;
	private Term oprnd2;

	public ExprTerm(Term oprnd1, int op, Term oprnd2) {
		this.oprnd1 = oprnd1;
		this.op = op;
		this.oprnd2 = oprnd2;
	}

	public ExprTerm(Term oprnd1, int op) {
		this.oprnd1 = oprnd1;
		this.op = op;
	}

	public Term getOprnd1() {
		return oprnd1;
	}

	public void setOprnd1(Term oprnd1) {
		this.oprnd1 = oprnd1;
	}

	public int getOp() {
		return op;
	}

	public void setOp(int op) {
		this.op = op;
	}

	public Term getOprnd2() {
		return oprnd2;
	}

	public void setOprnd2(Term oprnd2) {
		this.oprnd2 = oprnd2;
	}
	
	public String get(int op) {
		return opArr[op];
	}

	@Override
	public String toString() {
		String ret = "";

		if (oprnd2 == null) {
			ret += opArr[op] + oprnd1;
		} else {
			ret += oprnd1 + " " + opArr[op] + " " + oprnd2;
		}

		return ret;
	}
	
	private static String[] opArr = {"+", "-", "*", "/", "-",
	                                 ">", ">=", "<", "<=", "==", "!=",
	                                 "and", "or", "!"};
	
	public static final int ADD = 0;
	public static final int SUB = 1;
	public static final int MUL = 2;
	public static final int DIV = 3;
	public static final int UNARY = 4;
	
	public static final int GTHAN = 5;
	public static final int GEQUAL = 6;
	public static final int LTHAN = 7;
	public static final int LEQUAL = 8;
	public static final int EQUAL = 9;
	public static final int NOTEQUAL = 10;
	
	public static final int AND = 11;
	public static final int OR = 12;
	public static final int NOT = 13;
}
