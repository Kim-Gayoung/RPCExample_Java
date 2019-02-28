package com.example.systemf.starpc;

import com.example.systemf.ast.Type;

public class Let extends Term {
	private String id;
	private Type idTy;
	private Term t1;
	private Term t2;
	
	public Let(String id, Type idTy, Term t1, Term t2) {
		this.id = id;
		this.idTy = idTy;
		this.t1 = t1;
		this.t2 = t2;
	}

	public Let(String id, Term t1, Term t2) {
		this.id = id;
		this.t1 = t1;
		this.t2 = t2;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Type getIdTy() {
		return idTy;
	}

	public void setIdTy(Type idTy) {
		this.idTy = idTy;
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

	@Override
	public String toString() {
		if (idTy != null) {
			return "let " + id + ": " + idTy + " = " + t1 + " in\n\t" + t2 + "\nend";
		}
		else
			return "let " + id + " = " + t1 + " in\n\t" + t2 + "\nend";
	}
	
}
