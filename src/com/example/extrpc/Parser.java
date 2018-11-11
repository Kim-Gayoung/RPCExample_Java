package com.example.extrpc;

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
			else if (text.equalsIgnoreCase("let"))
				return Token.LET;
			else if (text.equalsIgnoreCase("in"))
				return Token.IN;
			else if (text.equalsIgnoreCase("end"))
				return Token.END;
			else if (text.equalsIgnoreCase("if"))
				return Token.IF;
			else if (text.equalsIgnoreCase("then"))
				return Token.THEN;
			else if (text.equalsIgnoreCase("else"))
				return Token.ELSE;
			else if (text.equalsIgnoreCase("and"))
				return Token.AND;
			else if (text.equalsIgnoreCase("or"))
				return Token.OR;
			else if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false"))
				return Token.BOOL;
			else
				return Token.ID; });
		
		pu.lex("\\+", text -> { return Token.ADD; });
		pu.lex("-", text -> { return Token.SUB; });
		pu.lex("\\*", text -> { return Token.MUL; });
		pu.lex("/", text -> { return Token.DIV; });

		pu.lex("((>=)|>)", text -> { 
			if (text.equals(">"))
				return Token.GTHAN;
			else
				return Token.GEQUAL; });
		pu.lex("((<=)|<)", text -> { 
			if (text.equals("<"))
				return Token.LTHAN;
			else
				return Token.LEQUAL; });
		pu.lex("==", text -> { return Token.EQUAL; });
		pu.lex("((!=)|!)", text -> {
			if (text.equals("!="))
				return Token.NOTEQ;
			else
				return Token.NOT;
		});
		
		pu.lex("=", text -> { return Token.ASSIGN; });
		
		pu.lex("\".*\"", text -> { return Token.STR; });
		pu.lex("\\^[cs]", text -> { return Token.LOC; });
		pu.lex("\\(", text -> { return Token.OPENPAREN; });
		pu.lex("\\)", text -> { return Token.CLOSEPAREN; });
		pu.lex("\\.", text -> { return Token.DOT; });
		pu.lex(";", text -> { return Token.SEMICOLON; });
		pu.lexEndToken("$", Token.END_OF_TOKEN);
		
		pu.ruleStartSymbol("TopLevel'");
		pu.rule("TopLevel' -> TopLevel", () -> { return pu.get(1); });
		pu.rule("TopLevel -> id = LExpr", () -> { return new TopLevel(new Var(pu.getText(1)), (Term) pu.get(3)); });
		pu.rule("TopLevel -> id = LExpr ; TopLevel", () -> { return new TopLevel(new Var(pu.getText(1)), (Term) pu.get(3), (TopLevel) pu.get(5)); });

		pu.rule("LExpr -> Expr", () -> { return pu.get(1); });
		pu.rule("LExpr -> lam loc Params . LExpr", () -> {
			Object tree = pu.get(5);
			return new Lam(getLoc(pu.getText(2)), (Params) pu.get(3), (Term) tree); });
		pu.rule("LExpr -> let id = LExpr in LExpr end", () -> { return new Let(new Var(pu.getText(2)), (Term) pu.get(4), (Term) pu.get(6)); });
		pu.rule("LExpr -> if Expr then LExpr else LExpr", () -> { return new If((Term) pu.get(2), (Term) pu.get(4), (Term) pu.get(6)); });
		
		pu.rule("Expr -> Expr Term", () -> { return new App((Term) pu.get(1), (Term) pu.get(2)); });
		pu.rule("Expr -> Cond", () -> { return pu.get(1); });
		
		pu.rule("Params -> ( )", () -> { return new Params(); });
		pu.rule("Params -> IDs", () -> { return pu.get(1); });
		pu.rule("IDs -> id", () -> { return new Params(new Var(pu.getText(1))); });
		pu.rule("IDs -> id IDs", () -> { return new Params(new Var(pu.getText(1)), (Params) pu.get(2)); });
		
		pu.rule("Cond -> LogicNot", () -> { return pu.get(1); });
		pu.rule("LogicNot -> ! LogicOr", () -> { return new Logical((Term) pu.get(2), Logical.NOT); });
		pu.rule("LogicNot -> LogicOr", () -> { return pu.get(1); });
		pu.rule("LogicOr -> LogicOr or LogicAnd", () -> { return new Logical((Term) pu.get(1), Logical.OR, (Term) pu.get(3)); });
		pu.rule("LogicOr -> LogicAnd", () -> { return pu.get(1); });
		pu.rule("LogicAnd -> LogicAnd and CompEqNeq", () -> { return new Logical((Term) pu.get(1), Logical.AND, (Term) pu.get(3)); });
		pu.rule("LogicAnd -> CompEqNeq", () -> { return pu.get(1); });
		
		pu.rule("CompEqNeq -> CompEqNeq == Comp", () -> { return new Comp((Term) pu.get(1), Comp.EQUAL, (Term) pu.get(3)); });
		pu.rule("CompEqNeq -> CompEqNeq != Comp", () -> { return new Comp((Term) pu.get(1), Comp.NOTEQUAL, (Term) pu.get(3)); });
		pu.rule("CompEqNeq -> Comp", () -> { return pu.get(1); });
		pu.rule("Comp -> Comp < ArithAddSub", () -> { return new Comp((Term) pu.get(1), Comp.LTHAN, (Term) pu.get(3)); });
		pu.rule("Comp -> Comp <= ArithAddSub", () -> { return new Comp((Term) pu.get(1), Comp.LEQUAL, (Term) pu.get(3)); });
		pu.rule("Comp -> Comp > ArithAddSub", () -> { return new Comp((Term) pu.get(1), Comp.GTHAN, (Term) pu.get(3)); });
		pu.rule("Comp -> Comp >= ArithAddSub", () -> { return new Comp((Term) pu.get(1), Comp.GEQUAL, (Term) pu.get(3)); });
		pu.rule("Comp -> ArithAddSub", () -> { return pu.get(1); });
		
		pu.rule("ArithAddSub -> ArithAddSub + ArithMulDiv", () -> {
			return new Arithmetic((Term) pu.get(1), Arithmetic.ADD, (Term) pu.get(3));
		});
		pu.rule("ArithAddSub -> ArithAddSub - ArithMulDiv", () -> {
			return new Arithmetic((Term) pu.get(1), Arithmetic.SUB, (Term) pu.get(3));
		});
		pu.rule("ArithAddSub -> ArithMulDiv", () -> { return pu.get(1); });
		pu.rule("ArithMulDiv -> ArithMulDiv * ArithUnary", () -> {
			return new Arithmetic((Term) pu.get(1), Arithmetic.MUL, (Term) pu.get(3));
		});
		pu.rule("ArithMulDiv -> ArithMulDiv / ArithUnary", () -> {
			return new Arithmetic((Term) pu.get(1), Arithmetic.DIV, (Term) pu.get(3));
		});
		pu.rule("ArithMulDiv -> ArithUnary", () -> { return pu.get(1); });
		pu.rule("ArithUnary -> - Term", () -> { return new Arithmetic((Term) pu.get(2), Arithmetic.UNARY); });
		pu.rule("ArithUnary -> Term", () -> { return pu.get(1); });
		
		pu.rule("Term -> id", () -> { return new Var(pu.getText(1)); });
		pu.rule("Term -> num", () -> { return new Num(Integer.parseInt(pu.getText(1))); });
		pu.rule("Term -> str", () -> { return new Str(pu.getText(1)); });
		pu.rule("Term -> bool", () -> { return new Bool(pu.getText(1)); });
		pu.rule("Term -> ( )", () -> { return new Unit(); });
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
