package com.example.extrpc;

import java.util.Set;
import java.util.stream.Collectors;

public class ForAll extends Type {
	private Set<Integer> locInts;
	private Set<Integer> tyInts;
	private Type ty;
	
	public ForAll(Set<Integer> locInts, Set<Integer> tyInts, Type ty) {
		this.locInts = locInts;
		this.tyInts = tyInts;
		this.ty = ty;
	}

	public Set<Integer> getLocInts() {
		return locInts;
	}

	public void setLocInts(Set<Integer> locInts) {
		this.locInts = locInts;
	}

	public Set<Integer> getTyInts() {
		return tyInts;
	}

	public void setTyInts(Set<Integer> tyInts) {
		this.tyInts = tyInts;
	}

	public Type getTy() {
		return ty;
	}

	public void setTy(Type ty) {
		this.ty = ty;
	}

	@Override
	public String toString() {
		return "ForAll [";
	}

	@Override
	protected Type clone() {
		return new ForAll(locInts.stream().collect(Collectors.toSet()),
						tyInts.stream().collect(Collectors.toSet()),
						ty.clone());
	}

}
