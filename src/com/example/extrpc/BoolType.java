package com.example.extrpc;

public class BoolType extends Type {

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof BoolType)
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return "boolean";
	}

	@Override
	protected Type clone() {
		return new BoolType();
	}
	
}
