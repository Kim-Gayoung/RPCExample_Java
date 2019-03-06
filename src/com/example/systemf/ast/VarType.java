package com.example.systemf.ast;

import org.json.simple.JSONObject;

public class VarType extends Type {
	private String var;

	public VarType(String var) {
		this.var = var;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof VarType) {
			VarType varTy = (VarType) arg0;
			
			return varTy.getVar() == this.var;
		}
		else
			return false;
	}

	@Override
	public String toString() {
		return var;
	}

	@Override
	protected Type clone() {
		return new VarType(var);
	}
	
	public static final String VarType = "VarType";
	
	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(VarType, new String(var));
		
		return jsonObject;
	}
}
