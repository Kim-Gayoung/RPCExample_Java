package com.example.systemf.ast;

import java.util.ArrayList;

public class PrimTerm extends Term {
	private ArrayList<Term> oprnds;
	private int op;

	public PrimTerm(ArrayList<Term> oprnds, int op) {
		this.oprnds = oprnds;
		this.op = op;
	}

	public ArrayList<Term> getOprnds() {
		return oprnds;
	}

	public void setOprnds(ArrayList<Term> oprnds) {
		this.oprnds = oprnds;
	}

	public int getOp() {
		return op;
	}

	public void setOp(int op) {
		this.op = op;
	}
	
	public String get(int i) {
		return opArr[i];
	}

	@Override
	public String toString() {
		String ret = "";

		if (oprnds.size() < 2)
			return opArr[op] + oprnds.get(0);
		else
			return oprnds.get(0) + " " + opArr[op] + " " + oprnds.get(1);
	}

	private static String[] opArr = { "+", "-", "*", "/", "-",
									  ">", ">=", "<", "<=", "==", "!=",
									  "and", "or", "!" };

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
