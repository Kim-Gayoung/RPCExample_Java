package com.example.systemf;

import java.util.ArrayList;

import com.example.systemf.ast.Type;

import javafx.util.Pair;

public class TyEnv {
	private ArrayList<Pair<String, Type>> pairList;

	public TyEnv() {
		pairList = new ArrayList<>();
	}

	public ArrayList<Pair<String, Type>> getPairList() {
		return pairList;
	}

	public void setPairList(ArrayList<Pair<String, Type>> pairList) {
		this.pairList = pairList;
	}

}
