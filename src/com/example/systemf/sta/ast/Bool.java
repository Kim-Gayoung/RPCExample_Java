package com.example.systemf.sta.ast;

import org.json.simple.JSONObject;

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
	
	public boolean getBool() {
		if (bool.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}

	public static final String Bool = "Bool";

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(Bool, new Boolean(getBool()));
		return jsonObject;
	}
}
