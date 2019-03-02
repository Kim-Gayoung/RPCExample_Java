package com.example.systemf.sta.ast;

import org.json.simple.JSONObject;

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

	public static final String Num = "Num";

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(Num, new Long(i));
		return jsonObject;
	}
}
