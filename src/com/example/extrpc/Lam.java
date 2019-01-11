package com.example.extrpc;

public class Lam extends Value {
	private Location loc;
	private String x;
	private Type idTy;
	private Term m;

	public Lam(Location loc, String x, Term m) {
		this.loc = loc;
		this.x = x;
		this.m = m;
	}

	public Lam(Location loc, String x, Type idTy, Term m) {
		this.loc = loc;
		this.x = x;
		this.idTy = idTy;
		this.m = m;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public Type getIdTy() {
		return idTy;
	}

	public void setIdTy(Type idTy) {
		this.idTy = idTy;
	}

	public Term getM() {
		return m;
	}

	public void setM(Term m) {
		this.m = m;
	}

	@Override
	public String toString() {
		if (idTy != null) {
			if (loc == Location.Polymorphic)
				return "lam" + " (" + x + ": " + idTy + ").\n" + m.toString();
			else
				return "lam^" + loc + "(" + x + ": " + idTy + ").\n" + m.toString();
		}
		else {
			if (loc == Location.Polymorphic)
				return "lam" + " (" + x + ").\n" + m.toString();
			else
				return "lam^" + loc + "(" + x + ").\n" + m.toString();
		}
	}
}
