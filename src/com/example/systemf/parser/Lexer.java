package com.example.systemf.parser;

import com.example.lib.CommonParserUtil;

public class Lexer {
	public Lexer(CommonParserUtil<Token> pu) {
		pu.lexEndToken("\\$", text -> { return Token.END_OF_TOKEN; });
		
		pu.lexKeyword("lam", text -> { return Token.LAM; });
		pu.lexKeyword("forall", text -> { return Token.FORALL; });
		pu.lexKeyword("let", text -> { return Token.LET; });
		pu.lexKeyword("in", text -> { return Token.IN; });
		pu.lexKeyword("end", text -> { return Token.END; });
		pu.lexKeyword("if", text -> { return Token.IF; });
		pu.lexKeyword("then", text -> { return Token.THEN; });
		pu.lexKeyword("else", text -> { return Token.ELSE; });
		pu.lexKeyword("and", text -> { return Token.AND; });
		pu.lexKeyword("or", text -> { return Token.OR; });
		pu.lexKeyword("True", text -> { return Token.BOOL; });
		pu.lexKeyword("False", text -> { return Token.BOOL; });
		pu.lexKeyword("Int", text -> { return Token.INTTYPE; });
		pu.lexKeyword("Unit", text -> { return Token.UNITTYPE; });
		pu.lexKeyword("Bool", text -> { return Token.BOOLTYPE; });
		pu.lexKeyword("String", text -> { return Token.STRTYPE; });
		pu.lexKeyword("tylam", text -> { return Token.TYLAM; });
				
		
		pu.lex("[ \t\n]", text -> { return null; }); 
		pu.lex("[cs]", text -> { return Token.LOC; });
		pu.lex("[0-9]+", text -> { return Token.NUM; });
		pu.lex("[_a-zA-Z]+[0-9]*", text -> { return Token.ID; });
		pu.lex("\\-\\>", text -> { return Token.ARROW; });
		
		pu.lex("\\+", text -> { return Token.ADD; });
		pu.lex("\\-", text -> { return Token.SUB; });
		pu.lex("\\*", text -> { return Token.MUL; });
		pu.lex("\\/", text -> { return Token.DIV; });
		
		pu.lex("==", text -> { return Token.EQUAL; });
		pu.lex("=", text -> { return Token.ASSIGN; });
		pu.lex(">=", text -> { return Token.GEQUAL; });
		pu.lex(">", text -> { return Token.GTHAN; });
		pu.lex("<=", text -> { return Token.LEQUAL; });
		pu.lex("\\<", text -> { return Token.LTHAN; });
		
		pu.lex("\\!\\=", text -> { return Token.NOTEQ; });
		pu.lex("\\!", text -> { return Token.NOT; });
		
		pu.lex("\"[^(\")]*\"", text -> { return Token.STR; });
		pu.lex("\\^", text -> { return Token.AT; });
		pu.lex("\\(", text -> { return Token.OPENPAREN; });
		pu.lex("\\)", text -> { return Token.CLOSEPAREN; });
		pu.lex("\\.", text -> { return Token.DOT; });
		pu.lex("\\;", text -> { return Token.SEMICOLON; });
		pu.lex("\\:", text -> { return Token.COLON; });
		
	}
}
