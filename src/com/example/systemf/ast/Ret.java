package com.example.systemf.ast;

public class Ret extends Term {
	private Value w;

	public Ret(Value w) {
		this.w = w;
	}

	public Value getW() {
		return w;
	}

	public void setW(Value w) {
		this.w = w;
	}

	@Override
	public String toString() {
		return "Ret(" + w + ")"; 
	}
	
}
