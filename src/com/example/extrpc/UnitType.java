package com.example.extrpc;

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
	
}
