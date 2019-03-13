package com.example.systemf.sta.ast;

import org.json.simple.JSONObject;

public class Var extends Value {
	private String var;

	public Var(String var) {
		this.var = var;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	@Override
	public String toString() {
		return var;
	}
	
	public static final String Var = "Var";
	
	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put(Var, var);
		
		return jsonObject;
	}
}
