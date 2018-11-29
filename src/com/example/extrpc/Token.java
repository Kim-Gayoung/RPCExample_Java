package com.example.extrpc;

import com.example.lib.ParserException;
import com.example.lib.TokenInterface;
import com.example.extrpc.Token;

public enum Token implements TokenInterface<Token> {
	END_OF_TOKEN("$"),
	OPENPAREN("("), CLOSEPAREN(")"),
	DOT("."), LOC("loc"),
	ADD("+"), SUB("-"), MUL("*"), DIV("/"),
	ASSIGN("="), EQUAL("=="), NOTEQ("!="),
	AND("and"), OR("or"), NOT("!"),
	GTHAN(">"), GEQUAL(">="), LTHAN ("<"), LEQUAL("<="),
	LAM("lam"), ID("id"), NUM("num"), STR("str"), BOOL("bool"),
	LET("let"), IN("in"), END("end"),
	IF("if"), THEN("then"), ELSE("else"), SEMICOLON(";");
	
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
	
	@Override
	public Token toToken(String s) throws ParserException {
		return Token.findToken(s);
	}
	@Override
	public String toString(Token tok) {
		return tok.getStrToken();
	}
}
