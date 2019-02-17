package com.example.systemf.ast;

public class Str extends Value {
	private String str;

	public Str(String str) {
		super();
		this.str = str;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return str;
	}

}
