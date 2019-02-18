package com.example.systemf.constraint;

import com.example.systemf.ast.TypedLocation;

public class CallableLoc extends Equ {
	private TypedLocation funLoc;
	private TypedLocation ctxLoc;
	
	public CallableLoc(TypedLocation funLoc, TypedLocation ctxLoc) {
		this.funLoc = funLoc;
		this.ctxLoc = ctxLoc;
	}

	public TypedLocation getFunLoc() {
		return funLoc;
	}

	public void setFunLoc(TypedLocation funLoc) {
		this.funLoc = funLoc;
	}

	public TypedLocation getCtxLoc() {
		return ctxLoc;
	}

	public void setCtxLoc(TypedLocation ctxLoc) {
		this.ctxLoc = ctxLoc;
	}
	
	@Override
	public String toString() {
		return funLoc + " |-> " + ctxLoc; 
	}
	
	
}
