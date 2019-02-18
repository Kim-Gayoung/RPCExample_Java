package com.example.systemf.ast;

public class All extends Term {
	private Type ty;
	private Term term;
	
	public All(Type ty, Term term) {
		this.ty = ty;
		this.term = term;
	}

	public Type getTy() {
		return ty;
	}

	public void setTy(Type ty) {
		this.ty = ty;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	@Override
	public String toString() {
		return "All " + ty + ". " + term; 
	}
	
}
