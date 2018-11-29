package com.example.extrpc;

public class StrType extends Type {

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof StrType)
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return "string";
	}

	@Override
	protected Type clone() {
		return new StrType();
	}
	
}
