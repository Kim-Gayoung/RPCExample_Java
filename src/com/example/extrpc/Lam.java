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
			return "lam" + loc + "(" + x + ": " + idTy + "). " + m.toString();
		}
		else {
			return "lam" + loc + "(" + x + "). " + m.toString();
		}
	}
}
