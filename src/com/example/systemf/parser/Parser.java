package com.example.systemf.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import com.example.lib.CommonParserUtil;
import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.systemf.ast.App;
import com.example.systemf.ast.Bool;
import com.example.systemf.ast.BoolType;
import com.example.systemf.ast.ForAll;
import com.example.systemf.ast.FunType;
import com.example.systemf.ast.If;
import com.example.systemf.ast.IntType;
import com.example.systemf.ast.Lam;
import com.example.systemf.ast.Let;
import com.example.systemf.ast.LibTerm;
import com.example.systemf.ast.LocType;
import com.example.systemf.ast.LocVarType;
import com.example.systemf.ast.Location;
import com.example.systemf.ast.Num;
import com.example.systemf.ast.PrimTerm;
import com.example.systemf.ast.Str;
import com.example.systemf.ast.StrType;
import com.example.systemf.ast.Tapp;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.Tylam;
import com.example.systemf.ast.Type;
import com.example.systemf.ast.Unit;
import com.example.systemf.ast.UnitType;
import com.example.systemf.ast.Var;
import com.example.systemf.ast.VarType;
import com.example.utils.TripleTup;

public class Parser {
	private CommonParserUtil<Token> pu;
	private static int n = 1;

	public Parser() throws IOException {
		pu = new CommonParserUtil<Token>();

		new Lexer(pu);

		pu.ruleStartSymbol("TopLevel'");
		pu.rule("TopLevel' -> TopLevel", () -> {
			System.out.println("TopLevel'");
			ArrayList<TripleTup<String, Type, Term>> list = (ArrayList<TripleTup<String, Type, Term>>) pu.get(1);
			TripleTup<String, Type, Term> mainTerm = null;

			for (TripleTup<String, Type, Term> t : list) {
				String id = t.getFirst();

				if (id.equals("main")) {
					mainTerm = t;
					list.remove(mainTerm);
				}
			}
			try {
				if (mainTerm == null)

					throw new ParserException("Program must have main function");

				else {
					Term main = mainTerm.getThird();
					Term ret = main;

					for (int i = list.size() - 1; i >= 0; i--) {
						TripleTup<String, Type, Term> t = list.get(i);

						if (t.getSecond() != null)
							ret = new Let(t.getFirst(), t.getSecond(), t.getThird(), ret);
						else
							ret = new Let(t.getFirst(), t.getThird(), ret);
					}

					return ret;
				}
			}
			catch (ParserException e) {
				e.printStackTrace();
				return null;
			}
		});
		pu.rule("TopLevel -> id OptType = LExpr", () -> {
			String id = pu.getText(1);
			Type ty = pu.get(2) != null ? (Type) pu.get(2) : null;
			Term t1 = (Term) pu.get(4);

			TripleTup<String, Type, Term> top = new TripleTup<>(id, ty, t1);

			ArrayList<TripleTup<String, Type, Term>> list = new ArrayList<>();
			list.add(top);

			return list;
		});
		pu.rule("TopLevel -> id OptType = LExpr ; TopLevel", () -> {
			String id = pu.getText(1);
			Type ty = pu.get(2) != null ? (Type) pu.get(2) : null;
			Term t1 = (Term) pu.get(4);
			ArrayList<TripleTup<String, Type, Term>> next = (ArrayList<TripleTup<String, Type, Term>>) pu.get(6);

			TripleTup<String, Type, Term> top = new TripleTup<>(id, ty, t1);

			ArrayList<TripleTup<String, Type, Term>> list = new ArrayList<>();
			list.add(top);
			list.addAll(next);

			return list;
		});
		pu.rule("LExpr -> lam OptLoc id OptType . LExpr", () -> {
			Location loc = pu.get(2) != null ? (Location) pu.get(2) : null;
			String id = pu.getText(3);
			Type ty = pu.get(4) != null ? (Type) pu.get(4) : null;
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
		pu.rule("LExpr -> Expr", () -> {
			return pu.get(1);
		});

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

			return new Tapp(term, ty);
		});
		pu.rule("Expr -> # id ( IdList )", () -> {
			return new LibTerm(pu.getText(2), (ArrayList<String>) pu.get(4));
		});
		pu.rule("Expr -> Cond", () -> {
			return pu.get(1);
		});

		pu.rule("Cond -> LogicNot", () -> {
			return pu.get(1);
		});
		pu.rule("LogicNot -> ! LogicOr", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(2));
			
			return new PrimTerm(oprnds, PrimTerm.NOT);
		});
		pu.rule("LogicNot -> LogicOr", () -> {
			return pu.get(1);
		});
		pu.rule("LogicOr -> LogicOr or LogicAnd", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.OR);
		});
		pu.rule("LogicOr -> LogicAnd", () -> {
			return pu.get(1);
		});
		pu.rule("LogicAnd -> LogicAnd and CompEqNeq", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.AND);
		});
		pu.rule("LogicAnd -> CompEqNeq", () -> {
			return pu.get(1);
		});

		pu.rule("CompEqNeq -> CompEqNeq == Comp", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.EQUAL);
		});
		pu.rule("CompEqNeq -> CompEqNeq != Comp", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.NOTEQUAL);
		});
		pu.rule("CompEqNeq -> Comp", () -> {
			return pu.get(1);
		});
		pu.rule("Comp -> Comp < ArithAddSub", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.LTHAN);
		});
		pu.rule("Comp -> Comp <= ArithAddSub", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.LEQUAL);
		});
		pu.rule("Comp -> Comp > ArithAddSub", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.GTHAN);
		});
		pu.rule("Comp -> Comp >= ArithAddSub", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.GEQUAL);
		});
		pu.rule("Comp -> ArithAddSub", () -> {
			return pu.get(1);
		});

		pu.rule("ArithAddSub -> ArithAddSub + ArithMulDiv", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.ADD);
		});
		pu.rule("ArithAddSub -> ArithAddSub - ArithMulDiv", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.SUB);
		});
		pu.rule("ArithAddSub -> ArithMulDiv", () -> {
			return pu.get(1);
		});
		pu.rule("ArithMulDiv -> ArithMulDiv * ArithUnary", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.MUL);
		});
		pu.rule("ArithMulDiv -> ArithMulDiv / ArithUnary", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(1));
			oprnds.add((Term) pu.get(3));
			
			return new PrimTerm(oprnds, PrimTerm.DIV);
		});
		pu.rule("ArithMulDiv -> ArithUnary", () -> {
			return pu.get(1);
		});
		pu.rule("ArithUnary -> - Term", () -> {
			ArrayList<Term> oprnds = new ArrayList<>();
			oprnds.add((Term) pu.get(2));
			
			return new PrimTerm(oprnds, PrimTerm.UNARY);
		});
		pu.rule("ArithUnary -> Term", () -> {
			return pu.get(1);
		});

		pu.rule("Term -> id", () -> {
			return new Var(pu.getText(1));
		});
		pu.rule("Term -> num", () -> {
			return new Num(Integer.parseInt(pu.getText(1)));
		});
		pu.rule("Term -> str", () -> {
			return new Str(pu.getText(1));
		});
		pu.rule("Term -> bool", () -> {
			return new Bool(pu.getText(1));
		});
		pu.rule("Term -> ( )", () -> {
			return new Unit();
		});
		pu.rule("Term -> ( LExpr )", () -> {
			return pu.get(2);
		});
		
		pu.rule("IdList -> ", () -> {
			return new ArrayList<>();
		});
		pu.rule("IdList -> id", () -> {
			ArrayList<String> idList = new ArrayList<>();
			idList.add(pu.getText(1));
			
			return idList;
		});
		pu.rule("IdList -> id , IdList", () -> {
			ArrayList<String> idList = new ArrayList<>();
			idList.add(pu.getText(1));
			idList.addAll((ArrayList<String>) pu.get(3));
			
			return idList;
		});

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
		ArrayList<TripleTup<String, Type, Term>> list = (ArrayList<TripleTup<String, Type, Term>>) pu.Parsing(r);
		TripleTup<String, Type, Term> mainTerm = null;

		for (int i = list.size() - 1; i >= 0; i--) {
			TripleTup<String, Type, Term> t = list.get(i);

			if (t.getFirst().equals("main")) {
				mainTerm = t;
				list.remove(i);
				break;
			}
		}

		try {
			if (mainTerm == null)
				throw new ParserException("Program must have main function");

			else {
				Term main = mainTerm.getThird();
				Term ret = main;

				for (int i = list.size() - 1; i >= 0; i--) {
					TripleTup<String, Type, Term> t = list.get(i);

					if (t.getSecond() != null)
						ret = new Let(t.getFirst(), t.getSecond(), t.getThird(), ret);
					else
						ret = new Let(t.getFirst(), t.getThird(), ret);
				}

				return ret;
			}
		}
		catch (ParserException e) {
			e.printStackTrace();
			return null;
		}
//		return (Term) pu.Parsing(r);
	}

	private Location getLoc(String loc) {
		if (loc.equals("s"))
			return Location.Server;
		else
			return Location.Client;
	}
}
