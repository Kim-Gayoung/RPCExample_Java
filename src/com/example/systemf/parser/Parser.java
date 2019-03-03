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
import com.example.systemf.ast.ForAll;
import com.example.systemf.ast.FunType;
import com.example.systemf.ast.If;
import com.example.systemf.ast.IntType;
import com.example.systemf.ast.Lam;
import com.example.systemf.ast.Let;
import com.example.systemf.ast.LocType;
import com.example.systemf.ast.LocVarType;
import com.example.systemf.ast.Location;
import com.example.systemf.ast.Num;
import com.example.systemf.ast.Str;
import com.example.systemf.ast.StrType;
import com.example.systemf.ast.TApp;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.Tylam;
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
			return (Term) pu.get(1);
		});
		pu.rule("TopLevel -> id OptType = LExpr", () -> {
			String id = pu.getText(1);
			Type ty = pu.get(2) != null? (Type) pu.get(2) : null;
			Term t1 = (Term) pu.get(4);
			
			return new Let(id, ty, t1, new Var(id));
		});
		pu.rule("TopLevel -> id OptType = LExpr ; TopLevel", () -> {
			String id = pu.getText(1);
			Type ty = pu.get(2) != null? (Type) pu.get(2) : null;
			Term t1 = (Term) pu.get(4);
			Term next = (Term) pu.get(6);
			
			if (ty != null)
				return new Let(id, ty, t1, next);
			else
				return new Let(id, t1, next);
		}); 
		pu.rule("LExpr -> lam OptLoc id OptType . LExpr", () -> {
			Location loc = pu.get(2) != null? (Location) pu.get(2) : null;
			String id = pu.getText(3);
			Type ty = pu.get(4) != null? (Type) pu.get(4) : null;
			Term body = (Term) pu.get(6);
			
			if (ty != null)
				return new Lam(loc, id, ty, body);
			else
				return new Lam(loc, id, body);
		});
		pu.rule("LExpr -> tylam id . LExpr", () -> {
			String strTy = pu.getText(2);
			Term body = (Term) pu.get(4);
			
			return new Tylam(new VarType(strTy), body);
		});
		pu.rule("LExpr -> let id OptType = LExpr in LExpr end", () -> {
			String id = pu.getText(2);
			Type ty = pu.get(3) != null ? (Type) pu.get(3) : null;
			Term t1 = (Term) pu.get(5);
			Term t2 = (Term) pu.get(7);
			
			if (ty != null)
				return new Let(id, ty, t1, t2);
			else
				return new Let(id, t1, t2);
		});
		pu.rule("LExpr -> if Expr then LExpr else LExpr", () -> {
			return new If((Term) pu.get(2), (Term) pu.get(4), (Term) pu.get(6));
		});
		pu.rule("LExpr -> Expr", () -> { return pu.get(1); });

		pu.rule("Expr -> Expr Term", () -> {
			return new App((Term) pu.get(1), (Term) pu.get(2));
		});
		pu.rule("Expr -> Expr ^ id ^ Term", () -> {
			Location loc = getLoc(pu.getText(3));
		
			return new App((Term) pu.get(1), (Term) pu.get(5), new LocType(loc));
		});
		pu.rule("Expr -> Expr [ Type ]", () -> {
			// TAPP
			Term term = (Term) pu.get(1);
			Type ty = (Type) pu.get(3);
			
			return new TApp(term, ty);
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
		
		pu.rule("Type -> ForAllType LocArrow Type", () -> {
			Type argTy = (Type) pu.get(1);
			Location loc = (Location) pu.get(2);
			Type retTy = (Type) pu.get(3);
			
			if (loc == Location.Polymorphic) {
				return new FunType(argTy, new LocVarType(n++), retTy);
			}
			else
				return new FunType(argTy, new LocType(loc), retTy);
		});
		pu.rule("Type -> ForAllType", () -> {
			return pu.get(1);
		});
		
		pu.rule("ForAllType -> forall id . ForAllType", () -> {
			String id = pu.getText(2);
			VarType tyId = new VarType(id);
			Type ty = (Type) pu.get(4);
			
			return new ForAll(tyId, ty);
		});
		pu.rule("ForAllType -> PrimaryType", () -> {
			return pu.get(1);
		});
		
		pu.rule("PrimaryType -> Unit", () -> {
			return new UnitType();
		});
		pu.rule("PrimaryType -> Int", () -> {
			return new IntType();
		});
		pu.rule("PrimaryType -> Bool", () -> {
			return new BoolType();
		}); 
		pu.rule("PrimaryType -> String", () -> {
			return new StrType();
		});
		pu.rule("PrimaryType -> id", () -> {
			return new VarType(pu.getText(1));
		});
		pu.rule("PrimaryType -> ( Type )", () -> {
			return pu.get(2);
		});
		
		pu.rule("LocArrow -> - id ->", () -> {
			Location loc = getLoc(pu.getText(2));
			
			return loc;
		});
		pu.rule("LocArrow -> ->", () -> {
			return Location.Polymorphic;
		});
		pu.rule("OptType -> : Type", () -> {
			return pu.get(2);
		});
		pu.rule("OptType -> ", () -> {
			return null;
		});
		pu.rule("OptLoc -> ^ id", () -> {
			return getLoc(pu.getText(2));
		});
		pu.rule("OptLoc -> ", () -> {
			return null;
		});
	}
	
	public Term Parsing(Reader r) throws ParserException, IOException, LexerException {
		return (Term) pu.Parsing(r);
	}

	private Location getLoc(String loc) {
		if (loc.equals("s"))
			return Location.Server;
		else
			return Location.Client;
	}
}
