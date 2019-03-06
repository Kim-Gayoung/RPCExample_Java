package com.example.systemf.stacs;

import com.example.systemf.sta.ast.Term;

public class Ctx {
	private String x;
	private Term t;

	public Ctx(String x, Term t) {
		this.x = x;
		this.t = t;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public Term getT() {
		return t;
	}

	public void setT(Term t) {
		this.t = t;
	}

}
