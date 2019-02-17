package com.example.systemf.parser;

import java.io.IOException;
import java.io.Reader;

import com.example.lib.CommonParserUtil;
import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.systemf.ast.App;
import com.example.systemf.ast.Bool;
import com.example.systemf.ast.BoolType;
import com.example.systemf.ast.ExprTerm;
import com.example.systemf.ast.If;
import com.example.systemf.ast.IntType;
import com.example.systemf.ast.Lam;
import com.example.systemf.ast.Let;
import com.example.systemf.ast.Location;
import com.example.systemf.ast.Num;
import com.example.systemf.ast.Str;
import com.example.systemf.ast.StrType;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.TopLevel;
import com.example.systemf.ast.Type;
import com.example.systemf.ast.Unit;
import com.example.systemf.ast.UnitType;
import com.example.systemf.ast.Var;
import com.example.systemf.ast.VarType;

public class Parser {
	private CommonParserUtil<Token> pu;
	private static int n = 1;
	
	public Parser() throws IOException {
		pu = new CommonParserUtil<Token>();
		
		new Lexer(pu);
		
		pu.ruleStartSymbol("TopLevel'");
		pu.rule("TopLevel' -> TopLevel", () -> { return pu.get(1); });
		pu.rule("TopLevel -> LExpr", () -> {
			Term top = (Term) pu.get(1);
			
			return new TopLevel(top);
		});
		pu.rule("TopLevel -> id = LExpr", () -> {
			String id = pu.getText(1);
			
			return new TopLevel(new Let(id, (Term) pu.get(3), new Var(id)));
		});
		pu.rule("TopLevel -> id : type = LExpr", () -> {
			String id = pu.getText(1);
			String strTy = pu.getText(3);
			Type ty = getType(strTy);
			Term t1 = (Term) pu.get(5);
			
			return new TopLevel(new Let(id, ty, t1, new Var(id)));
		});
		pu.rule("TopLevel -> id = LExpr ; TopLevel" , () -> {
			String id = pu.getText(1);
			TopLevel next = (TopLevel) pu.get(5);
			
			return new TopLevel(new Let(id, (Term) pu.get(3), new Var(id)), next);
		});
		pu.rule("TopLevel -> id : type = LExpr ; TopLevel", () -> {
			String id = pu.getText(1);
			String strTy = pu.getText(3);
			Type ty = getType(strTy);
			Term t1 = (Term) pu.get(5);
			TopLevel next = (TopLevel) pu.get(7);
			
			return new TopLevel(new Let(id, ty, t1, new Var(id)), next);
		}); 
		pu.rule("LExpr -> Expr", () -> { return pu.get(1); });
		pu.rule("LExpr -> lam loc id . LExpr", () -> {
			String strLoc = pu.getText(2);
			Location loc = getLoc(strLoc);
			String id = pu.getText(3);
			Term body = (Term) pu.get(5);
			
			return new Lam(loc, id, body);
		});
		pu.rule("LExpr -> lam loc id : type . LExpr", () -> {
			String strLoc = pu.getText(2);
			Location loc = getLoc(strLoc);
			String id = pu.getText(3);
			String strTy = pu.getText(5);
			Type ty = getType(strTy);
			Term body = (Term) pu.get(7);
			
			return new Lam(loc, id, ty, body);
		});
		pu.rule("LExpr -> lam loc ( ) . LExpr", () -> {
			String strLoc = pu.getText(2);
			Location loc = getLoc(strLoc);
			String id = "_tempVar";
			Term body = (Term) pu.get(6);
			
			return new Lam(loc, id, new UnitType(), body);
		});
		pu.rule("LExpr -> let id = LExpr in LExpr end", () -> {
			return new Let(pu.getText(2), (Term) pu.get(4), (Term) pu.get(6));
		});
		pu.rule("LExpr -> let id : type = LExpr in LExpr end", () -> {
			String id = pu.getText(2);
			String strTy = pu.getText(4);
			Type ty = getType(strTy);
			Term t1 = (Term) pu.get(6);
			Term t2 = (Term) pu.get(8);
			
			return new Let(id, ty, t1, t2);
		});
		pu.rule("LExpr -> if Expr then LExpr else LExpr", () -> {
			return new If((Term) pu.get(2), (Term) pu.get(4), (Term) pu.get(6));
		});

		pu.rule("Expr -> Expr Term", () -> {
			return new App((Term) pu.get(1), (Term) pu.get(2));
		});
		pu.rule("Expr -> Cond", () -> { return pu.get(1); });

		pu.rule("Cond -> LogicNot", () -> { return pu.get(1); });
		pu.rule("LogicNot -> ! LogicOr", () -> {
			return new ExprTerm((Term) pu.get(2), ExprTerm.NOT);
		});
		pu.rule("LogicNot -> LogicOr", () -> { return pu.get(1); });
		pu.rule("LogicOr -> LogicOr or LogicAnd", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.OR, (Term) pu.get(3));
		});
		pu.rule("LogicOr -> LogicAnd", () -> { return pu.get(1); });
		pu.rule("LogicAnd -> LogicAnd and CompEqNeq", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.AND, (Term) pu.get(3));
		});
		pu.rule("LogicAnd -> CompEqNeq", () -> { return pu.get(1); });

		pu.rule("CompEqNeq -> CompEqNeq == Comp", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.EQUAL, (Term) pu.get(3));
		});
		pu.rule("CompEqNeq -> CompEqNeq != Comp", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.NOTEQUAL, (Term) pu.get(3));
		});
		pu.rule("CompEqNeq -> Comp", () -> { return pu.get(1); });
		pu.rule("Comp -> Comp < ArithAddSub", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.LTHAN, (Term) pu.get(3));
		});
		pu.rule("Comp -> Comp <= ArithAddSub", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.LEQUAL, (Term) pu.get(3));
		});
		pu.rule("Comp -> Comp > ArithAddSub", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.GTHAN, (Term) pu.get(3));
		});
		pu.rule("Comp -> Comp >= ArithAddSub", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.GEQUAL, (Term) pu.get(3));
		});
		pu.rule("Comp -> ArithAddSub", () -> { return pu.get(1); });

		pu.rule("ArithAddSub -> ArithAddSub + ArithMulDiv", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.ADD, (Term) pu.get(3));
		});
		pu.rule("ArithAddSub -> ArithAddSub - ArithMulDiv", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.SUB, (Term) pu.get(3));
		});
		pu.rule("ArithAddSub -> ArithMulDiv", () -> { return pu.get(1); });
		pu.rule("ArithMulDiv -> ArithMulDiv * ArithUnary", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.MUL, (Term) pu.get(3));
		});
		pu.rule("ArithMulDiv -> ArithMulDiv / ArithUnary", () -> {
			return new ExprTerm((Term) pu.get(1), ExprTerm.DIV, (Term) pu.get(3));
		});
		pu.rule("ArithMulDiv -> ArithUnary", () -> { return pu.get(1); });
		pu.rule("ArithUnary -> - Term", () -> {
			return new ExprTerm((Term) pu.get(2), ExprTerm.UNARY);
		});
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
	
	private Type getType(String ty) {
		if (ty.equals("int"))
			return new IntType();
		else if (ty.equals("string"))
			return new StrType();
		else if (ty.equals("bool"))
			return new BoolType();
		else if (ty.equals("unit"))
			return new UnitType();
		else
			return new VarType(n++);
	}
}
