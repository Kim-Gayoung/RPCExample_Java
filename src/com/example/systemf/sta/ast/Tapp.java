package com.example.systemf.sta.ast;

import com.example.systemf.ast.Type;

public class Tapp extends Term {
	private Term fun;
	private Type ty;
	
	public Tapp(Term fun, Type ty) {
		this.fun = fun;
		this.ty = ty;
	}

	public Term getFun() {
		return fun;
	}

	public void setFun(Term fun) {
		this.fun = fun;
	}

	public Type getTy() {
		return ty;
	}

	public void setTy(Type ty) {
		this.ty = ty;
	}

	@Override
	public String toString() {
		return "(" + fun + " " + ty + ")";
	}
	
	
}
