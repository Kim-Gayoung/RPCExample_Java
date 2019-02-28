package com.example.systemf.starpc;

public class Num extends Value {
	private int i;

	public Num(int i) {
		super();
		this.i = i;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	@Override
	public String toString() {
		return i + "";
	}
}
