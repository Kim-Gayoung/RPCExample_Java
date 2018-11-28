package com.example.extrpc;

public class VarType extends Type {
	private int var;

	public VarType(int var) {
		this.var = var;
	}

	public int getVar() {
		return var;
	}

	public void setVar(int var) {
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
		return "a" + var;
	}
	
}
