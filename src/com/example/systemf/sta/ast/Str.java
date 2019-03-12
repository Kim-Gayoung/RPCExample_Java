package com.example.systemf.sta.ast;

import org.json.simple.JSONObject;

public class Str extends Value {
	private String str;

	public Str(String str) {
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
		return "\"" + str + "\"";
	}

	public static final String Str = "Str";

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(Str, new String(str));
		return jsonObject;
	}
}
