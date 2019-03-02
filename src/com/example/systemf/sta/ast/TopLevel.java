package com.example.systemf.sta.ast;

public class TopLevel extends Term {
	private Term top;
	private TopLevel next;
	
	public TopLevel(Term top) {
		this.top = top;
	}
	
	public TopLevel(Term top, TopLevel next) {
		this.top = top;
		this.next = next;
	}
	
	public Term getTop() {
		return top;
	}
	
	public TopLevel getNext() {
		return next;
	}
	
	public void setTop(Term top) {
		this.top = top;
	}
	
	public void setNext(TopLevel next) {
		this.next = next;
	}
	
	@Override
	public String toString() {
		String ret = top.toString();
		
		if (next != null)
			ret += ";\n" + next;
		
		return ret;
	}
}
