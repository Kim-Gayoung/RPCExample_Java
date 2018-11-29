package com.example.lib;

public interface TokenInterface<Token> {
	Token toToken(String s) throws ParserException;
	String toString(Token tok);
}
