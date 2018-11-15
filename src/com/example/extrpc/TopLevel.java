package com.example.extrpc;

public class TopLevel extends Term {
	private Var id;
	private Type idTy;
	private Term body;
	private TopLevel next;
	
	public TopLevel(Var id, Term body, TopLevel next) {
		this.id = id;
		this.body = body;
		this.next = next;
	}
	
	public TopLevel(Var id, Term body) {
		this.id = id;
		this.body = body;
	}

	public TopLevel(Var id, Type idTy, Term body) {
		this.id = id;
		this.idTy = idTy;
		this.body = body;
	}
	
	public TopLevel(Var id, Type idTy, Term body, TopLevel next) {
		this.id = id;
		this.idTy = idTy;
		this.body = body;
		this.next = next;
	}
	
	public Var getId() {
		return id;
	}

	public void setId(Var id) {
		this.id = id;
	}

	public Term getBody() {
		return body;
	}

	public void setBody(Term body) {
		this.body = body;
	}

	public TopLevel getNext() {
		return next;
	}

	public void setNext(TopLevel next) {
		this.next = next;
	}
	
	public Type getIdTy() {
		return idTy;
	}

	public void setIdTy(Type idTy) {
		this.idTy = idTy;
	}

	@Override
	public String toString() {
		String ret = id + " = " + body;
		
		if (next != null)
			ret += ";\n" + next;
		
		return ret;
	}
	
}
