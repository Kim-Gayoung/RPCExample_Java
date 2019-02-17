package com.example.systemf.ast;

public class Bool extends Value {
	private String bool;

	public Bool(String bool) {
		this.bool = bool;
	}

	public String isBool() {
		return bool;
	}

	public void setBool(String bool) {
		this.bool = bool;
	}

	@Override
	public String toString() {
		return bool;
	}

}
