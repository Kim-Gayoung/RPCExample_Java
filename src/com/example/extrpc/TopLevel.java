package com.example.extrpc;

public class TopLevel extends Term {
	private Var id;
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

	@Override
	public String toString() {
		String ret = id + " = " + body;
		
		if (next != null)
			ret += ";\n" + next;
		
		return ret;
	}
	
}
