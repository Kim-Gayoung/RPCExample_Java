package com.example.extrpc;

public class LocType extends TypedLocation {
	private Location loc;

	public LocType(Location loc) {
		this.loc = loc;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof LocType) {
			LocType locTy = (LocType) arg0;
			
			return locTy.getLoc().equals(this.loc);
		}
		else
			return false;
	}

	@Override
	public String toString() {
		return loc.getLoc();
	}
	
}
