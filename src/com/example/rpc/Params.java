package com.example.rpc;

public class Params extends Value {
	private Var id;
	private Params ids;
	
	public Params(Var id, Params ids) {
		this.id = id;
		this.ids = ids;
	}
	
	public Params(Var id) {
		this.id = id;
	}
	
	public Params() {
		
	}

	public Var getId() {
		return id;
	}

	public void setId(Var id) {
		this.id = id;
	}

	public Params getIds() {
		return ids;
	}

	public void setIds(Params ids) {
		this.ids = ids;
	}

	@Override
	public String toString() {
		String ret = "";
		if (id != null)
			ret += id;
		if (ids != null)
			ret += " " + ids;
		
		return ret;
	}
	
}
