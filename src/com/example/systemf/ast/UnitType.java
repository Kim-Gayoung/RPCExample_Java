package com.example.systemf.ast;

public class UnitType extends Type {

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof UnitType)
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return "Unit";
	}

	@Override
	protected Type clone() {
		return new UnitType();
	}
	
}
