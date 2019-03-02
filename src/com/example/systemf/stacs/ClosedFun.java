package com.example.systemf.stacs;

import java.util.ArrayList;

import com.example.systemf.ast.Location;
import com.example.systemf.ast.Type;
import com.example.systemf.sta.ast.Term;

public class ClosedFun {
	private ArrayList<Type> ts;
	private ArrayList<String> zs;
	private Location loc;
	private ArrayList<String> xs;
	private Term m;

	public ClosedFun(ArrayList<Type> ts, ArrayList<String> zs, Location loc, ArrayList<String> xs, Term m) {
		this.ts = ts;
		this.zs = zs;
		this.loc = loc;
		this.xs = xs;
		this.m = m;
	}

	public ArrayList<String> getZs() {
		return zs;
	}

	public void setZs(ArrayList<String> zs) {
		this.zs = zs;
	}

	public ArrayList<Type> getTs() {
		return ts;
	}

	public void setTs(ArrayList<Type> ts) {
		this.ts = ts;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public ArrayList<String> getXs() {
		return xs;
	}

	public void setXs(ArrayList<String> xs) {
		this.xs = xs;
	}

	public Term getM() {
		return m;
	}

	public void setM(Term m) {
		this.m = m;
	}

	@Override
	public String toString() {
		String ret = "{";
		int cnt = 0;

		for (String z : zs) {
			ret += z;

			if (cnt < zs.size() - 1) {
				ret += " ";
				cnt++;
			}
		}

		ret += "} {";
		cnt = 0;

		for (Type t : ts) {
			ret += t;

			if (cnt < ts.size() - 1) {
				ret += " ";
				cnt++;
			}
		}

		ret += "} lam^";

		if (loc == Location.Client)
			ret += "c";
		else
			ret += "s";

		ret += "(";
		cnt = 0;

		for (String x : xs) {
			ret += x;

			if (cnt < xs.size() - 1) {
				ret += " ";
				cnt++;
			}
		}

		ret += ")." + m.toString();

		return ret;
	}

}
