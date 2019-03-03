package com.example.systemf.sta.ast;

import com.example.systemf.ast.Type;

public class TopLevel extends Term {
	private String id;
	private Type idTy;
	private Term term;
	private TopLevel next;

	public TopLevel(Term term) {
		this.term = term;
	}

	public TopLevel(String id, Term term) {
		this.id = id;
		this.term = term;
	}

	public TopLevel(Term term, TopLevel next) {
		this.term = term;
		this.next = next;
	}
	
	public TopLevel(String id, Type idTy, Term term) {
		this.id = id;
		this.idTy = idTy;
		this.term = term;
	}

	public TopLevel(String id, Term term, TopLevel next) {
		this.id = id;
		this.term = term;
		this.next = next;
	}
	
	public TopLevel(String id, Type idTy, Term term, TopLevel next) {
		this.id = id;
		this.idTy = idTy;
		this.term = term;
		this.next = next;
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

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public TopLevel getNext() {
		return next;
	}

	public void setNext(TopLevel next) {
		this.next = next;
	}


	@Override
	public String toString() {
		String ret = "";

		if (id != null)
			ret += id;
		if (idTy != null)
			ret += ": " + idTy;
		
		if (term != null)
			ret += " = " + term;
		else
			ret += term;

		if (next != null)
			ret += ";\n" + next;

		return ret;
	}
}
