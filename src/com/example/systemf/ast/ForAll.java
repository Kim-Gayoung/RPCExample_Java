package com.example.systemf.ast;

public class ForAll extends Type {
	private Type tyId;
	private TypedLocation tyedLoc;
	private Type ty;
	
	public ForAll(Type tyId, Type ty) {
		this.tyId = tyId;
		this.ty = ty;
	}
	
	public ForAll(TypedLocation tyedLoc, Type ty) {
		this.tyedLoc = tyedLoc;
		this.ty = ty;
	}

	public Type getTyId() {
		return tyId;
	}

	public void setTyId(Type tyId) {
		this.tyId = tyId;
	}

	public TypedLocation getTyedLoc() {
		return tyedLoc;
	}

	public void setTyedLoc(TypedLocation tyedLoc) {
		this.tyedLoc = tyedLoc;
	}

	public Type getTy() {
		return ty;
	}

	public void setTy(Type ty) {
		this.ty = ty;
	}

	@Override
	public String toString() {
		if (tyId != null)
			return "forall " + tyId + "." + ty;
		else
			return "forall " + tyedLoc + "." + ty;
	}

	@Override
	protected Type clone() {
		if (tyId != null)
			return new ForAll(tyId.clone(), ty.clone());
		else
			return new ForAll(tyedLoc.clone(), ty.clone());
	}

}
