package com.example.extrpc;

public class FunType extends Type {
	private Type funTy;
	private TypedLocation loc;
	private Type argTy;

	public FunType(Type funTy, TypedLocation loc, Type argTy) {
		this.funTy = funTy;
		this.loc = loc;
		this.argTy = argTy;
	}

	public Type getFunTy() {
		return funTy;
	}

	public void setFunTy(Type funTy) {
		this.funTy = funTy;
	}

	public TypedLocation getLoc() {
		return loc;
	}

	public void setLoc(TypedLocation loc) {
		this.loc = loc;
	}

	public Type getArgTy() {
		return argTy;
	}

	public void setArgTy(Type argTy) {
		this.argTy = argTy;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof FunType) {
			FunType argFunTy = (FunType) arg0;
			
			return argFunTy.getFunTy().equals(this.funTy) && argFunTy.getArgTy().equals(this.argTy) && argFunTy.getLoc().equals(this.loc);
		}
		else
			return false;
	}

	@Override
	public String toString() {
		return "(" + funTy + "-" + loc + "->" + argTy + ")";
	}

	@Override
	protected Type clone() {
		return new FunType(funTy.clone(), loc.clone(), argTy.clone());
	}
	
	
}
