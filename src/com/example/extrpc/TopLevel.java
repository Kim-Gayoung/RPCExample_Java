package com.example.extrpc;

public class TopLevel extends Term {
	private Term term;
	private TopLevel next;

	public TopLevel(Term term) {
		this.term = term;
	}

	public TopLevel(Term term, TopLevel next) {
		this.term = term;
		this.next = next;
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
		String ret = term.toString();

		if (next != null)
			ret += ";\n" + next;

		return ret;
	}

}
