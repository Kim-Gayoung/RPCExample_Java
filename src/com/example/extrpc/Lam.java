package com.example.extrpc;

public class Lam extends Value {
	private Location loc;
	private Params x;
	private Term m;

	public Lam(Location loc, Params x, Term m) {
		this.loc = loc;
		this.x = x;
		this.m = m;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public Params getX() {
		return x;
	}

	public void setX(Params x) {
		this.x = x;
	}

	public Term getM() {
		return m;
	}

	public void setM(Term m) {
		this.m = m;
	}

	@Override
	public String toString() {
		String ret = "";

		if (loc == Location.Client)
			ret += "lam^c ";
		else
			ret += "lam^s ";

		ret += "(" + x + "). " + m.toString();
		
		return ret;
	}
}
