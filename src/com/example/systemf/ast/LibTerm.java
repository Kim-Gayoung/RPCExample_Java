package com.example.systemf.ast;

import java.util.ArrayList;

public class LibTerm extends Term {
	private String funName;
	private ArrayList<String> args;

	public LibTerm(String funName, ArrayList<String> args) {
		this.funName = funName;
		this.args = args;
	}

	public String getFunName() {
		return funName;
	}

	public void setFunName(String funName) {
		this.funName = funName;
	}

	public ArrayList<String> getArgs() {
		return args;
	}

	public void setArgs(ArrayList<String> args) {
		this.args = args;
	}

}
