package com.example.rpc;

public class Arithmetic extends Term {
	private Term oprnd1;
	private String op;
	private Term oprnd2;
	
	public Arithmetic(Term oprnd1, String op, Term oprnd2) {
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
		String ret = "";
		
		if (op.equals(UNARY)) 
			ret = op + oprnd1;
		else
			ret = oprnd1 + " " + op + " " + oprnd2;
		
		return ret;
	}
	
	public static final String ADD = "+";
	public static final String SUB = "-";
	public static final String MUL = "*";
	public static final String DIV = "/";
	public static final String RES = "%";
	
	public static final String UNARY = "-";
}
