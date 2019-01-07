package com.example.extrpc;

public class FunType extends Type {
	private Type argTy;
	private TypedLocation loc;
	private Type retTy;

	public FunType(Type argTy, TypedLocation loc, Type retTy) {
		this.argTy = argTy;
		this.loc = loc;
		this.retTy = retTy;
	}

	public Type getArgTy() {
		return argTy;
	}

	public void setArgTy(Type argTy) {
		this.argTy = argTy;
	}

	public TypedLocation getLoc() {
		return loc;
	}

	public void setLoc(TypedLocation loc) {
		this.loc = loc;
	}

	public Type getRetTy() {
		return retTy;
	}

	public void setRetTy(Type retTy) {
		this.retTy = retTy;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof FunType) {
			FunType argFunTy = (FunType) arg0;
			
			return argFunTy.getArgTy().equals(this.argTy) && argFunTy.getRetTy().equals(this.retTy) && argFunTy.getLoc().equals(this.loc);
		}
		else
			return false;
	}

	@Override
	public String toString() {
		return "(" + argTy + "-" + loc + "->" + retTy + ")";
	}

	@Override
	protected Type clone() {
		return new FunType(argTy.clone(), loc.clone(), retTy.clone());
	}
	
	
}
