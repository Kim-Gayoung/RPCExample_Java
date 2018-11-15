package com.example.extrpc;

public class Let extends Term {
	private Var id;
	private Type idTy;
	private Term t1;
	private Term t2;

	public Let(Var id, Term t1, Term t2) {
		this.id = id;
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public Let(Var id, Type idTy, Term t1, Term t2) {
		this.id = id;
		this.idTy = idTy;
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public Var getId() {
		return id;
	}

	public void setId(Var id) {
		this.id = id;
	}

	public Term getT1() {
		return t1;
	}

	public void setT1(Term t1) {
		this.t1 = t1;
	}

	public Term getT2() {
		return t2;
	}

	public void setT2(Term t2) {
		this.t2 = t2;
	}

	public Type getIdTy() {
		return idTy;
	}

	public void setIdTy(Type idTy) {
		this.idTy = idTy;
	}

	@Override
	public String toString() {
		if (idTy != null) {
			return "let " + id + ": " + idTy + " = " + t1 + " in " + t2 + " end";
		}
		else {
			return "let " + id + " = " + t1 + " in " + t2 + " end";
		}
	}
	
}
