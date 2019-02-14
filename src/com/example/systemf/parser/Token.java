package com.example.systemf.parser;

import com.example.lib.ParserException;
import com.example.lib.TokenInterface;

public enum Token implements TokenInterface<Token> {
	END_OF_TOKEN("$"),
	OPENPAREN("("), CLOSEPAREN(")"), SEMICOLON(";"), COLON(":"),
	DOT("."), LOC("loc"),
	ADD("+"), SUB("-"), MUL("*"), DIV("/"),
	ASSIGN("="), EQUAL("=="), NOTEQ("!="),
	AND("and"), OR("or"), NOT("!"),
	GTHAN(">"), GEQUAL(">="), LTHAN("<"), LEQUAL("<="),
	LAM("lam"), ID("id"), NUM("num"), STR("str"), BOOL("bool"),
	LET("let"), IN("in"), END("end"),
	IF("if"), THEN("then"), ELSE("else"),
	TYPE("type");
	
	private String strToken;
	
	private Token(String strToken) {
		this.strToken = strToken;
	}

	@Override
	public Token toToken(String s) throws ParserException {
		for (Token token: Token.values()) {
			if (token.strToken.equals(s))
				return token;
		}
		
		throw new ParserException("Token(" + s + ") not expected.");
	}

	@Override
	public String toString(Token tok) {
		return tok.strToken;
	}
}
