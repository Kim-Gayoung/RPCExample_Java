package com.example.rpc;

import java.io.IOException;
import java.io.Reader;

import com.example.lib.CommonParserUtil;
import com.example.lib.LexerException;
import com.example.lib.ParserException;

public class Parser {
	private CommonParserUtil pu;
	
	public Parser() throws IOException, LexerException {
		pu = new CommonParserUtil();
//		pu = new CommonParserUtil("grammar_rules.txt", "action_table.txt", "goto_table.txt");

		pu.lex("[ \t\n]", text -> { return null; });
		pu.lex("[0-9]+", text -> { return Token.NUM; });
		pu.lex("[a-zA-Z]+[0-9]*", text -> {
			if (text.equalsIgnoreCase("lam"))
					return Token.LAM;
			else
					return Token.ID; });
		pu.lex("\\^[cs]", text -> { return Token.LOC; });
		pu.lex("\\(", text -> { return Token.OPENPAREN; });
		pu.lex("\\)", text -> { return Token.CLOSEPAREN; });
		pu.lex("\\.", text -> { return Token.DOT; });
		pu.lexEndToken("$", Token.END_OF_TOKEN);
		
		pu.ruleStartSymbol("LExpr'");
		pu.rule("LExpr' -> LExpr", () -> { return pu.get(1); });
		
		pu.rule("LExpr -> Expr", () -> { return pu.get(1); });
		pu.rule("LExpr -> lam loc Params . LExpr", () -> {
			Object tree = pu.get(5);
			return new Lam(getLoc(pu.getText(2)), pu.getText(3), (Term) tree); });
		pu.rule("LExpr -> let id = Lexpr end", () -> { return new Let(); });
		pu.rule("LExpr -> let id = Lexpr in LExpr end", () -> { return new Let(); });
		pu.rule("LExpr -> if Cond then LExpr else LExpr", () -> { return new If(); });
		
		pu.rule("Expr -> Expr Term", () -> { return new App((Term) pu.get(1), (Term) pu.get(2)); });
		pu.rule("Expr -> Cond", () -> { return pu.get(1); });
		
		pu.rule("Params -> ( )", () -> { return new Params(); });
		pu.rule("Params -> ( IDs )", () -> { return new Params(); });
		pu.rule("IDs -> id", () -> { return new Var(pu.getText(1)); });
		pu.rule("IDs -> id IDs", () -> { });
		
		pu.rule("Cond -> LogicOr", () -> { return pu.get(1); });
		pu.rule("LogicOr -> LogicOr or LogicAnd", tb);
		pu.rule("LogicOr -> LogicAnd", tb);
		pu.rule("LogicAnd -> LogicAnd and CompEqNeq", tb);
		pu.rule("LogicAnd -> CompEqNeq", tb);
		
		pu.rule("CompEqNeq -> CompEqNeq == Comp", tb);
		pu.rule("CompEqNeq -> CompEqNeq != Comp", tb);
		pu.rule("CompEqNeq -> Comp", tb);
		pu.rule("Comp -> Comp < ArithAddSub", tb);
		pu.rule("Comp -> Comp <= ArithAddSub", tb);
		pu.rule("Comp -> Comp > ArithAddSub", tb);
		pu.rule("Comp -> Comp >= ArithAddSub", tb);
		pu.rule("Comp -> ArithAddSub", tb);
		
		pu.rule("ArithAddSub -> ArithAddSub + ArithMulDiv", tb);
		pu.rule("ArithAddSub -> ArithAddSub - ArithMulDiv", tb);
		pu.rule("ArithAddSub -> ArithMulDiv", tb);
		pu.rule("ArithMulDiv -> ArithMulDiv * ArithUnary", tb);
		pu.rule("ArithMulDiv -> ArithMulDiv / ArithUnary", tb);
		pu.rule("ArithMulDiv -> ArithUnary", tb);
		pu.rule("ArithUnary -> - Term", tb);
		pu.rule("ArithUnary -> Term", tb);
		
		pu.rule("Term -> id", () -> { return new Var(pu.getText(1)); });
		pu.rule("Term -> num", () -> { return new Const(Integer.parseInt(pu.getText(1))); });
		pu.rule("Term -> str", tb);
		pu.rule("Term -> bool", tb);
		pu.rule("Term -> ( )", tb);
		pu.rule("Term -> ( LExpr )", () -> { return pu.get(2); });
	}
	
	public Term Parsing(Reader r) throws ParserException, IOException, LexerException {
		return (Term) pu.Parsing(r);
	}
	
	private Location getLoc(String loc) {
		if (loc.equals("^s"))
			return Location.Server;
		else
			return Location.Client;
	}
}
