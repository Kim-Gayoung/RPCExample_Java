package com.example.rpc;

import com.example.lib.ParserException;

public enum Token {
	END_OF_TOKEN("$"),
	OPENPAREN("("), CLOSEPAREN(")"),
	DOT("."), LOC("loc"),
	ADD("+"), SUB("-"), MUL("*"), DIV("/"),
	ASSIGN("="), EQUAL("=="), NOTEQ("!="),
	GTHAN(">"), GEQUAL(">="), LTHAN ("<"), LEQUAL("<="),
	LAM("lam"), ID("id"), NUM("num"), STR("str"), BOOL("bool"),
	LET("let"), IN("in"), END("end"),
	IF("if"), THEN("then"), ELSE("else");
	
	private String strToken;
	
	Token (String strToken) {
		this.strToken = strToken;
	}
	public String getStrToken() {
		return strToken;
	}
	
	public static Token findToken(String strToken) throws ParserException {
		for (Token t: Token.values()) {
			if (t.getStrToken().equals(strToken))
				return t;
		}
		throw new ParserException(strToken + " not expected.");
	}
}
