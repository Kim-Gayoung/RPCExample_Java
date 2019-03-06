package com.example.systemf.sta.ast;

import org.json.simple.JSONObject;

public class Unit extends Value {

	public Unit() {

	}

	@Override
	public String toString() {
		return "()";
	}

	public static final String Unit = "Unit";

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(Unit, new String(""));
		return jsonObject;
	}
}
