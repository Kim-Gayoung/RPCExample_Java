package com.example.typerpc;

public class FunType extends Type {
	private Type left;
	private TypedLocation loc;
	private Type right;
	
	public FunType(Type left, TypedLocation loc, Type right) {
		this.left = left;
		this.loc = loc;
		this.right = right;
	}
	public Type getLeft() {
		return left;
	}
	public void setLeft(Type left) {
		this.left = left;
	}
	public TypedLocation getLoc() {
		return loc;
	}
	public void setLoc(TypedLocation loc) {
		this.loc = loc;
	}
	public Type getRight() {
		return right;
	}
	public void setRight(Type right) {
		this.right = right;
	}
	
	@Override
	public String toString() {
		String ret = "(" + left.toString() + "-" + loc.toString() + " -> " + right.toString() + ")";
		return ret;
	}
	
}
