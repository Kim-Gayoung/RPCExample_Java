package com.example.systemf;

public class TypeCheckException extends Exception {
	private String msg;

	public TypeCheckException(String msg) {
		this.msg = msg;
		System.err.println(msg);
	}
}
