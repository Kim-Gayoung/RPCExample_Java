package com.example.systemf;

public class CompException extends Exception {
	private String msg;

	public CompException(String msg) {
		this.msg = msg;
		
		System.err.println(msg);
	}
	
}
