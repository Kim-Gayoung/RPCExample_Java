package com.example.extrpc;

import java.util.ArrayList;

import com.example.utils.QuadTup;
import com.example.utils.TripleTup;

import javafx.util.Pair;

public class Infer {

	private static Pair<TyEnv, Integer> initEnv() {
		int i = 1;
		TyEnv env = new TyEnv();
		Pair<TyEnv, Integer> generalLibraryEnv = generalLibrary(i);
		Pair<TyEnv, Integer> dbLibraryEnv = dbLibrary(generalLibraryEnv.getValue());

		env.getPairList().addAll(generalLibraryEnv.getKey().getPairList());
		env.getPairList().addAll(dbLibraryEnv.getKey().getPairList());

		return new Pair<>(env, generalLibraryEnv.getValue());
	}

	private static Pair<TyEnv, Integer> generalLibrary(int i) {
		// isNothing, fromJust
		// openFile, closeFile, writeFile, readFile
		// readConsole, writeConsole
		TyEnv env = new TyEnv();

		env.getPairList().add(new Pair<>("isNothing", new FunType(new StrType(), new LocVarType(i), new BoolType())));
		env.getPairList().add(new Pair<>("fromJust", new FunType(new StrType(), new LocVarType(i + 1), new StrType())));
		i = i + 2;

		env.getPairList().add(new Pair<>("openFile", new FunType(new StrType(), new LocVarType(i),
				new FunType(new StrType(), new LocVarType(i + 1), new StrType()))));
		env.getPairList()
				.add(new Pair<>("closeFile", new FunType(new StrType(), new LocVarType(i + 2), new UnitType())));
		env.getPairList().add(new Pair<>("writeFile", new FunType(new StrType(), new LocVarType(i + 3),
				new FunType(new StrType(), new LocVarType(i + 4), new StrType()))));
		env.getPairList().add(new Pair<>("readFile", new FunType(new StrType(), new LocVarType(i + 5), new StrType())));
		i = i + 6;

		env.getPairList().add(new Pair<>("readConsole", new FunType(new UnitType(), new LocVarType(i), new StrType())));
		env.getPairList()
				.add(new Pair<>("writeConsole", new FunType(new StrType(), new LocVarType(i + 1), new UnitType())));
		i = i + 2;

		// toString, reverse: String -> String
		// append: String -> (String -> String)
		// length: String -> Int
		// getHour, getYear, getMonth, getDay, getDate: Unit->Int
		env.getPairList().add(new Pair<>("toString", new FunType(new VarType(i), new LocVarType(i), new StrType())));
		env.getPairList().add(new Pair<>("reverse", new FunType(new StrType(), new LocVarType(i + 1), new StrType())));
		i = i + 2;

		env.getPairList().add(new Pair<>("append", new FunType(new StrType(), new LocVarType(i),
				new FunType(new StrType(), new LocVarType(i), new StrType()))));
		i = i + 1;

		env.getPairList().add(new Pair<>("length", new FunType(new StrType(), new LocVarType(i), new IntType())));
		i = i + 1;

		env.getPairList().add(new Pair<>("getHour", new FunType(new UnitType(), new LocVarType(i), new IntType())));
		env.getPairList().add(new Pair<>("getYear", new FunType(new UnitType(), new LocVarType(i + 1), new IntType())));
		env.getPairList()
				.add(new Pair<>("getMonth", new FunType(new UnitType(), new LocVarType(i + 2), new IntType())));
		env.getPairList().add(new Pair<>("getDay", new FunType(new UnitType(), new LocVarType(i + 3), new IntType())));
		env.getPairList().add(new Pair<>("getDate", new FunType(new UnitType(), new LocVarType(i + 4), new IntType())));
		i = i + 5;

		return new Pair<>(env, i);
	}

	private static Pair<TyEnv, Integer> dbLibrary(int i) {
		// createRecord, updateRecord, insertRecord, deleteRecord, query
		TyEnv env = new TyEnv();
		LocType loc = new LocType(Location.Server);

		env.getPairList().add(new Pair<>("createRecord", new FunType(new VarType(i), loc, new VarType(i+1))));
		env.getPairList().add(new Pair<>("insertRecord", new FunType(new VarType(i+2), loc, new VarType(i+3))));
		env.getPairList().add(new Pair<>("updateRecord", new FunType(new VarType(i+4), loc, new VarType(i+5))));
		env.getPairList().add(new Pair<>("deleteRecord", new FunType(new VarType(i+6), loc, new VarType(i+7))));
		env.getPairList().add(new Pair<>("query", new FunType(new VarType(i+8), loc, new VarType(i+9))));
		i = i + 10;

		return new Pair<>(env, i);
	}

	public static TopLevel infer(Term m) {
		Pair<TyEnv, Integer> init = initEnv();
		QuadTup<TopLevel, Type, Equations, Integer> quadGenCst = genCstTopLevel(init.getValue(), (TopLevel) m,
				init.getKey());
		Equations equs1 = solve(quadGenCst.getThird());
		TopLevel tym = substTopLevel(quadGenCst.getFirst(), equs1);

		return tym;
	}

	public static Type tylookup(String x, TyEnv tyenv) {
		for (Pair<String, Type> p : tyenv.getPairList()) {
			if (p.getKey().equals(x))
				return p.getValue();
		}

		return null;
	}

	public static QuadTup<TopLevel, Type, Equations, Integer> genCstTopLevel(int n, TopLevel top, TyEnv tyenv) {
		Pair<String, Type> tmpIdTy = new Pair<>(top.getId().getVar(), new VarType(n));
		TyEnv tmpEnv = new TyEnv();
		tmpEnv.getPairList().addAll((ArrayList<Pair<String, Type>>) tyenv.getPairList().clone());
		tmpEnv.getPairList().add(tmpIdTy);
		
		QuadTup<Term, Type, Equations, Integer> constraints1 = genCst(n + 1, top.getBody(), tmpEnv);
		tyenv.getPairList().add(new Pair<>(top.getId().getVar(), constraints1.getSecond()));

		if (top.getNext() != null) {
			QuadTup<TopLevel, Type, Equations, Integer> constraints2 = genCstTopLevel(constraints1.getFourth(),
					top.getNext(), tyenv);

			Equations constraints = new Equations();
			constraints.getEqus().addAll(constraints1.getThird().getEqus());
			constraints.getEqus().addAll(constraints2.getThird().getEqus());
			constraints.getEqus().add(new EquTy(tmpIdTy.getValue(), constraints1.getSecond()));

			TopLevel tyTopLevel = new TopLevel(top.getId(), constraints1.getSecond(), constraints1.getFirst(),
					constraints2.getFirst());

			// toplevel list의 타입..?
			return new QuadTup<>(tyTopLevel, constraints2.getSecond(), constraints, constraints2.getFourth());
		}
		else {
			Equations constraints = new Equations();
			constraints.getEqus().addAll(constraints1.getThird().getEqus());
			constraints.getEqus().add(new EquTy(tmpIdTy.getValue(), constraints1.getSecond()));

			TopLevel tyTopLevel = new TopLevel(top.getId(), constraints1.getSecond(), constraints1.getFirst());

			return new QuadTup<>(tyTopLevel, constraints1.getSecond(), constraints, constraints1.getFourth());
		}

	}

	public static QuadTup<Term, Type, Equations, Integer> genCst(int n, Term t, TyEnv tyenv) {
		QuadTup<Term, Type, Equations, Integer> ret;

		if (t instanceof Unit) {
			ret = new QuadTup<>(t, new UnitType(), new Equations(), n);

			return ret;
		}
		else if (t instanceof Num) {
			ret = new QuadTup<>(t, new IntType(), new Equations(), n);

			return ret;
		}
		else if (t instanceof Str) {
			ret = new QuadTup<>(t, new StrType(), new Equations(), n);

			return ret;
		}
		else if (t instanceof Bool) {
			ret = new QuadTup<>(t, new BoolType(), new Equations(), n);

			return ret;
		}
		else if (t instanceof Var) {
			Var tVar = (Var) t;
			Type varTy = tylookup(tVar.getVar(), tyenv);

			ret = new QuadTup<>(tVar, varTy, new Equations(), n);

			return ret;
		}
		else if (t instanceof Lam) {
			Lam tLam = (Lam) t;

			Type argTy = new VarType(n);
			
			TyEnv tyenv1 = new TyEnv();
			ArrayList<Pair<String, Type>> pairList = tyenv.getPairList();

			tyenv1.setPairList(pairList);
			tyenv1.getPairList().add(0, new Pair<>(tLam.getX(), argTy));

			QuadTup<Term, Type, Equations, Integer> quad = genCst(n + 1, tLam.getM(), tyenv1);
			FunType funTy = new FunType(argTy, new LocType(tLam.getLoc()), quad.getSecond());

			ret = new QuadTup<>(new Lam(tLam.getLoc(), tLam.getX(), argTy, quad.getFirst()), funTy, quad.getThird(),
					quad.getFourth());
			
			tyenv1.getPairList().remove(0);

			return ret;
		}
		else if (t instanceof App) {
			App tApp = (App) t;

			QuadTup<Term, Type, Equations, Integer> fun = genCst(n, tApp.getFun(), tyenv);
			QuadTup<Term, Type, Equations, Integer> arg = genCst(fun.getFourth(), tApp.getArg(), tyenv);

			int k = arg.getFourth();
			TypedLocation loc = new LocVarType(k);
			Type retTy = new VarType(k);

			Equations constraints = new Equations();
			constraints.getEqus().addAll(fun.getThird().getEqus());
			constraints.getEqus().addAll(arg.getThird().getEqus());
			constraints.getEqus().add(new EquTy(fun.getSecond(), new FunType(arg.getSecond(), loc, retTy)));

			ret = new QuadTup<>(new App(fun.getFirst(), arg.getFirst(), loc), retTy, constraints, k + 2);

			return ret;
		}
		else if (t instanceof Let) {
			Let tLet = (Let) t;

			Pair<String, Type> tmpIdTy = new Pair<>(tLet.getId().getVar(),
					new FunType(new VarType(n), new LocVarType(n), new VarType(n + 1)));
			TyEnv cloneEnv = new TyEnv();
			cloneEnv.setPairList((ArrayList<Pair<String, Type>>) tyenv.getPairList().clone());
			cloneEnv.getPairList().add(tmpIdTy);

			QuadTup<Term, Type, Equations, Integer> t1Quad = genCst(n + 2, tLet.getT1(), cloneEnv);
			tyenv.getPairList().add(new Pair<>(tLet.getId().getVar(), t1Quad.getSecond()));
			QuadTup<Term, Type, Equations, Integer> t2Quad = genCst(t1Quad.getFourth(), tLet.getT2(), tyenv);

			Equations constraints = new Equations();
			constraints.getEqus().addAll(t1Quad.getThird().getEqus());
			constraints.getEqus().addAll(t2Quad.getThird().getEqus());

			ret = new QuadTup<>(new Let(tLet.getId(), t1Quad.getSecond(), t1Quad.getFirst(), t2Quad.getFirst()),
					t2Quad.getSecond(), constraints, t2Quad.getFourth());

			return ret;
		}
		else if (t instanceof If) {
			If tIf = (If) t;

			QuadTup<Term, Type, Equations, Integer> cond = genCst(n, tIf.getCond(), tyenv);
			QuadTup<Term, Type, Equations, Integer> thenCst = genCst(cond.getFourth(), tIf.getThenT(), tyenv);
			QuadTup<Term, Type, Equations, Integer> elseCst = genCst(thenCst.getFourth(), tIf.getElseT(), tyenv);

			Equ constraint1 = new EquTy(cond.getSecond(), new BoolType());
			Equ constraint2 = new EquTy(thenCst.getSecond(), elseCst.getSecond());

			Equations constraints = new Equations();
			constraints.getEqus().addAll(cond.getThird().getEqus());
			constraints.getEqus().addAll(thenCst.getThird().getEqus());
			constraints.getEqus().addAll(elseCst.getThird().getEqus());
			constraints.getEqus().add(constraint1);
			constraints.getEqus().add(constraint2);

			ret = new QuadTup<>(new If(cond.getFirst(), thenCst.getFirst(), elseCst.getFirst()), thenCst.getSecond(),
					constraints, elseCst.getFourth());

			return ret;
		}
		else if (t instanceof Arithmetic) {
			Arithmetic tArith = (Arithmetic) t;

			QuadTup<Term, Type, Equations, Integer> oprnd1 = genCst(n, tArith.getOprnd1(), tyenv);

			if (tArith.getOprnd2() != null) {
				QuadTup<Term, Type, Equations, Integer> oprnd2 = genCst(oprnd1.getFourth(), tArith.getOprnd2(), tyenv);
				Equ constraint1 = new EquTy(oprnd1.getSecond(), new IntType());
				Equ constraint2 = new EquTy(oprnd2.getSecond(), new IntType());

				Equations constraints = new Equations();
				constraints.getEqus().addAll(oprnd1.getThird().getEqus());
				constraints.getEqus().addAll(oprnd2.getThird().getEqus());
				constraints.getEqus().add(constraint1);
				constraints.getEqus().add(constraint2);

				ret = new QuadTup<>(new Arithmetic(oprnd1.getFirst(), tArith.getOp(), oprnd2.getFirst()), new IntType(),
						constraints, oprnd2.getFourth());
			}
			else {
				Equ constraint = new EquTy(oprnd1.getSecond(), new IntType());

				Equations constraints = new Equations();
				constraints.getEqus().addAll(oprnd1.getThird().getEqus());
				constraints.getEqus().add(constraint);

				ret = new QuadTup<>(new Arithmetic(oprnd1.getFirst(), tArith.getOp()), new IntType(), constraints,
						oprnd1.getFourth());
			}

			return ret;
		}
		else if (t instanceof Comp) {
			Comp tComp = (Comp) t;

			QuadTup<Term, Type, Equations, Integer> oprnd1 = genCst(n, tComp.getOprnd1(), tyenv);
			QuadTup<Term, Type, Equations, Integer> oprnd2 = genCst(oprnd1.getFourth(), tComp.getOprnd2(), tyenv);

			Equ constraint = new EquTy(oprnd1.getSecond(), oprnd2.getSecond());
			Equations constraints = new Equations();

			constraints.getEqus().addAll(oprnd1.getThird().getEqus());
			constraints.getEqus().addAll(oprnd2.getThird().getEqus());
			constraints.getEqus().add(constraint);

			ret = new QuadTup<>(new Comp(oprnd1.getFirst(), tComp.getOp(), oprnd2.getFirst()), new BoolType(),
					constraints, oprnd2.getFourth());

			return ret;
		}
		else if (t instanceof Logical) {
			Logical tLogic = (Logical) t;

			QuadTup<Term, Type, Equations, Integer> oprnd1 = genCst(n, tLogic.getOprnd1(), tyenv);

			if (tLogic.getOprnd2() != null) {
				QuadTup<Term, Type, Equations, Integer> oprnd2 = genCst(oprnd1.getFourth(), tLogic.getOprnd2(), tyenv);

				Equ constraint1 = new EquTy(oprnd1.getSecond(), new BoolType());
				Equ constraint2 = new EquTy(oprnd2.getSecond(), new BoolType());

				Equations constraints = new Equations();
				constraints.getEqus().addAll(oprnd1.getThird().getEqus());
				constraints.getEqus().addAll(oprnd2.getThird().getEqus());
				constraints.getEqus().add(constraint1);
				constraints.getEqus().add(constraint2);

				ret = new QuadTup<>(new Logical(oprnd1.getFirst(), tLogic.getOp(), oprnd2.getFirst()), new BoolType(),
						constraints, oprnd2.getFourth());
			}
			else {
				Equ constraint = new EquTy(oprnd1.getSecond(), new BoolType());

				Equations constraints = new Equations();
				constraints.getEqus().addAll(oprnd1.getThird().getEqus());
				constraints.getEqus().add(constraint);

				ret = new QuadTup<>(new Logical(oprnd1.getFirst(), tLogic.getOp()), new BoolType(), constraints,
						oprnd1.getFourth());
			}

			return ret;
		}
		else
			return null;
	}

	public static Equations solve(Equations equs) {
		while (true) {
			Pair<Equations, Boolean> p1 = unifyEqus(equs);
			Pair<Equations, Boolean> p2 = mergeAll(p1.getKey());
			Pair<Equations, Boolean> p3 = propagate(p2.getKey());

			if (p1.getValue() || p2.getValue() || p3.getValue())
				equs = p3.getKey();
			else
				return equs;
		}
	}

	public static Pair<Equations, Boolean> unifyEqus(Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();
		ArrayList<Equ> retList = new ArrayList<>();
		boolean changed = false;

		if (equList == null || equList.isEmpty())
			return new Pair<>(new Equations(retList), changed);
		else {
			for (Equ equ : equList) {
				Pair<Equations, Boolean> p1 = unify(equ);
				changed = changed || p1.getValue();

				retList.addAll(p1.getKey().getEqus());
			}

			return new Pair<>(new Equations(retList), changed);
		}
	}

	public static Pair<Equations, Boolean> unify(Equ equ) {
		if (equ instanceof EquTy) {
			EquTy equTy = (EquTy) equ;

			return unify_(equTy.getTy1(), equTy.getTy2());
		}
		else if (equ instanceof EquLoc) {
			EquLoc equLoc = (EquLoc) equ;

			return unifyLoc_(equLoc.getTyloc1(), equLoc.getTyloc2());
		}
		return null;
	}

	public static Pair<Equations, Boolean> unify_(Type ty1, Type ty2) {
//		System.out.println(ty1 + ", " + ty2);
		Pair<Equations, Boolean> retPair;
		// 타입 추가 필요
		if (ty1 instanceof IntType) {
			IntType intTy1 = (IntType) ty1;

			if (ty2 instanceof IntType) {
				IntType intTy2 = (IntType) ty2;

				retPair = new Pair<>(new Equations(), false);

				return retPair;
			}
			else if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, intTy1));

				retPair = new Pair<>(new Equations(equList), true);

				return retPair;
			}
		}
		else if (ty1 instanceof UnitType) {
			UnitType unitTy1 = (UnitType) ty1;

			if (ty2 instanceof UnitType) {
				retPair = new Pair<>(new Equations(), false);

				return retPair;
			}
			else if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, unitTy1));

				retPair = new Pair<>(new Equations(equList), true);

				return retPair;
			}
		}
		else if (ty1 instanceof BoolType) {
			BoolType boolTy1 = (BoolType) ty1;

			if (ty2 instanceof BoolType) {
				retPair = new Pair<>(new Equations(), false);

				return retPair;
			}
			else if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, boolTy1));

				retPair = new Pair<>(new Equations(equList), true);

				return retPair;
			}
		}
		else if (ty1 instanceof StrType) {
			StrType strTy1 = (StrType) ty1;

			if (ty2 instanceof StrType) {
				retPair = new Pair<>(new Equations(), false);

				return retPair;
			}
			else if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, strTy1));

				retPair = new Pair<>(new Equations(equList), true);

				return retPair;
			}
		}
		else if (ty1 instanceof VarType) {
			VarType varTy1 = (VarType) ty1;

			ArrayList<Equ> equList = new ArrayList<>();
			equList.add(new EquTy(varTy1, ty2));

			retPair = new Pair<>(new Equations(equList), false);

			return retPair;
		}
		else if (ty1 instanceof FunType) {
			FunType funTy1 = (FunType) ty1;

			if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, funTy1));

				retPair = new Pair<>(new Equations(equList), true);

				return retPair;
			}
			else if (ty2 instanceof FunType) {
				FunType funTy2 = (FunType) ty2;

				Pair<Equations, Boolean> p1 = unify_(funTy1.getFunTy(), funTy2.getFunTy());
				Pair<Equations, Boolean> p2 = unifyLoc_(funTy1.getLoc(), funTy2.getLoc());
				Pair<Equations, Boolean> p3 = unify_(funTy1.getArgTy(), funTy2.getArgTy());

				ArrayList<Equ> equList = new ArrayList<>();
				equList.addAll(p1.getKey().getEqus());
				equList.addAll(p2.getKey().getEqus());
				equList.addAll(p3.getKey().getEqus());

				retPair = new Pair<>(new Equations(equList), p1.getValue() || p2.getValue() || p3.getValue());

				return retPair;
			}
		}
		return null;
	}

	public static Pair<Equations, Boolean> unifyLoc_(TypedLocation tyloc1, TypedLocation tyloc2) {
		ArrayList<Equ> equList = new ArrayList<>();
		Pair<Equations, Boolean> retPair;

		if (tyloc1 instanceof LocVarType) {
			LocVarType locvarty1 = (LocVarType) tyloc1;

			equList.add(new EquLoc(locvarty1, tyloc2));

			retPair = new Pair<>(new Equations(equList), false);

			return retPair;
		}
		else if (tyloc1 instanceof LocType) {
			LocType locty1 = (LocType) tyloc1;

			if (tyloc2 instanceof LocVarType) {
				LocVarType locvarty2 = (LocVarType) tyloc2;

				equList.add(new EquLoc(locvarty2, locty1));

				retPair = new Pair<>(new Equations(equList), true);

				return retPair;
			}
			else if (tyloc2 instanceof LocType) {
				LocType locty2 = (LocType) tyloc2;

				if (locty1.getLoc() == locty2.getLoc()) {
					retPair = new Pair<>(new Equations(), true);

					return retPair;
				}
			}
		}
		return null;
	}

	public static Pair<Equations, Boolean> mergeAll(Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();

		Equations retEqus = new Equations();
		Pair<Equations, Boolean> retPair;

		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, false);

			return retPair;
		}
		else {
			Equ equ = equList.get(0);
			equList.remove(equ);

			TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equ, new Equations(equList));
			Pair<Equations, Boolean> p = mergeAll(merg.getSecond());

			retEqus.getEqus().add(equ);
			retEqus.getEqus().addAll(merg.getFirst().getEqus());
			retEqus.getEqus().addAll(p.getKey().getEqus());

			retPair = new Pair<>(retEqus, merg.getThird() || p.getValue());

			return retPair;
		}
	}

	public static TripleTup<Equations, Equations, Boolean> mergeTheRest(Equ equ, Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();

		ArrayList<Equ> retList = new ArrayList<>();
		TripleTup<Equations, Equations, Boolean> retTrip;

		if (equList == null || equList.isEmpty()) {
			retTrip = new TripleTup<>(new Equations(), new Equations(), false);
			return retTrip;
		}
		else {
			Equ e = equList.get(0);
			equList.remove(e);

			if (equ instanceof EquTy && e instanceof EquTy) {
				EquTy equty1 = (EquTy) equ;
				EquTy equty2 = (EquTy) e;

				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equty1, new Equations(equList));

				if (equty1.getTy1() == equty2.getTy1()) {
					Pair<Equations, Boolean> p = unify(new EquTy(equty1.getTy2(), equty2.getTy2()));
					retList = new ArrayList<>();
					retList.addAll(p.getKey().getEqus());
					retList.addAll(merg.getFirst().getEqus());

					retTrip = new TripleTup<>(new Equations(retList), merg.getSecond(),
							p.getValue() || merg.getThird());

					return retTrip;
				}
				else {
					retList = new ArrayList<>();
					retList.add(equty2);
					retList.addAll(merg.getSecond().getEqus());

					retTrip = new TripleTup<>(merg.getFirst(), new Equations(retList), merg.getThird());

					return retTrip;
				}
			}
			else if (equ instanceof EquLoc && e instanceof EquLoc) {
				EquLoc equloc1 = (EquLoc) equ;
				EquLoc equloc2 = (EquLoc) e;

				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equloc1, new Equations(equList));

				if (equloc1.getTyloc1() == equloc2.getTyloc1()) {
					Pair<Equations, Boolean> p = unify(new EquLoc(equloc1.getTyloc2(), equloc2.getTyloc2()));

					retList = new ArrayList<>();
					retList.addAll(p.getKey().getEqus());
					retList.addAll(merg.getFirst().getEqus());

					retTrip = new TripleTup<>(new Equations(retList), merg.getSecond(),
							p.getValue() || merg.getThird());

					return retTrip;
				}
				else {
					retList = new ArrayList<>();
					retList.addAll(merg.getSecond().getEqus());
					retList.add(equloc2);

					retTrip = new TripleTup<>(merg.getFirst(), new Equations(retList), merg.getThird());

					return retTrip;
				}
			}
			else {
				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equ, new Equations(equList));

				retList = new ArrayList<>();
				retList.add(e);
				retList.addAll(merg.getSecond().getEqus());

				retTrip = new TripleTup<>(merg.getFirst(), new Equations(retList), merg.getThird());

				return retTrip;
			}
		}
	}

	public static Pair<Equations, Boolean> propagate(Equations equs) {
		Pair<Equations, Boolean> prop = propagate_(equs, equs);

		return prop;
	}

	public static Pair<Equations, Boolean> propagate_(Equations equs1, Equations equs2) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		Equations cloneEqus = new Equations((ArrayList<Equ>) equs1.getEqus().clone());
		boolean changed = false;

		ArrayList<Equ> equList1 = equs1.getEqus();

		if (equList1 == null || equList1.isEmpty()) {
			retEqus.getEqus().addAll(equs2.getEqus());
			retPair = new Pair<>(retEqus, changed);

			return retPair;
		}
		else {
			Equ equ = cloneEqus.getEqus().get(0);
			cloneEqus.getEqus().remove(equ);

			if (equ instanceof EquTy) {
				EquTy equty = (EquTy) equ;

				if (equty.getTy1() instanceof VarType) {
					VarType varty1 = (VarType) equty.getTy1();
					Pair<Equations, Boolean> p1 = propagateTy(varty1.getVar(), equty.getTy2(), equs2);
					Pair<Equations, Boolean> p2 = propagate_(cloneEqus, p1.getKey());
					retEqus.getEqus().addAll(p2.getKey().getEqus());
					changed = changed || p1.getValue() || p2.getValue();
				}
			}
			else if (equ instanceof EquLoc) {
				EquLoc equloc = (EquLoc) equ;

				if (equloc.getTyloc1() instanceof LocVarType) {
					LocVarType locvarty1 = (LocVarType) equloc.getTyloc1();

					Pair<Equations, Boolean> p1 = propagateLoc(locvarty1.getVar(), equloc.getTyloc2(), equs2);
					Pair<Equations, Boolean> p2 = propagate_(cloneEqus, p1.getKey());
					retEqus.getEqus().addAll(p2.getKey().getEqus());
					changed = changed || p1.getValue() || p2.getValue();
				}
			}

			retPair = new Pair<>(retEqus, changed);
			return retPair;
		}
	}

	public static Pair<Equations, Boolean> propagateTy(int i, Type ity, Equations equs) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		boolean changed = false;

		ArrayList<Equ> equList = equs.getEqus();

		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, changed);

			return retPair;
		}
		else {
			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;

					Type ty1 = /* TypedRPCMain. */subst(equty.getTy2(), i, ity);
					changed = changed || !ty1.equals(equty.getTy2());
					retEqus.getEqus().add(new EquTy(equty.getTy1(), ty1));
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;

					retEqus.getEqus().add(equloc);
				}
			}
			retPair = new Pair<>(retEqus, changed);

			return retPair;
		}
	}

	public static Pair<Equations, Boolean> propagateLoc(int i, TypedLocation ilocty, Equations equs) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		boolean changed = false;

		ArrayList<Equ> equList = equs.getEqus();

		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, changed);
			return retPair;
		}
		else {
			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;

					retEqus.getEqus().add(equty);
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;

					TypedLocation tyloc1 = /* TypedRPCMain. */substTyLoc(equloc.getTyloc2(), i, ilocty);
					changed = changed || !equloc.getTyloc2().equals(tyloc1);
					retEqus.getEqus().add(new EquLoc(equloc.getTyloc1(), tyloc1));
				}
			}
			retPair = new Pair<>(retEqus, changed);

			return retPair;
		}
	}

	public static TopLevel substTopLevel(TopLevel t, Equations equs) {
		Term body = substTerm(t.getBody(), equs);

		if (t.getNext() != null) {
			TopLevel next = substTopLevel(t.getNext(), equs);

			return new TopLevel(t.getId(), t.getIdTy(), body, next);
		}
		else {
			return new TopLevel(t.getId(), t.getIdTy(), body);
		}
	}

	public static Term substTerm(Term t, Equations equs) {
		if (t instanceof Unit) {
			Unit tUnit = (Unit) t;

			return tUnit;
		}
		else if (t instanceof Num) {
			Num tNum = (Num) t;

			return tNum;
		}
		else if (t instanceof Str) {
			Str tStr = (Str) t;

			return tStr;
		}
		else if (t instanceof Bool) {
			Bool tBool = (Bool) t;

			return tBool;
		}
		else if (t instanceof Var) {
			Var tVar = (Var) t;

			return tVar;
		}
		else if (t instanceof Lam) {
			Lam tLam = (Lam) t;

			Type ty = substTyEqus(tLam.getIdTy(), equs);
			Term m = substTerm(tLam.getM(), equs);

			return new Lam(tLam.getLoc(), tLam.getX(), ty, m);
		}
		else if (t instanceof App) {
			App tApp = (App) t;

			Term fun = substTerm(tApp.getFun(), equs);
			TypedLocation loc = substLocEqus(tApp.getLoc(), equs);
			Term arg = substTerm(tApp.getArg(), equs);

			return new App(fun, arg, loc);
		}
		else if (t instanceof Let) {
			Let tLet = (Let) t;

			Type ty = substTyEqus(tLet.getIdTy(), equs);
			Term t1 = substTerm(tLet.getT1(), equs);
			Term t2 = substTerm(tLet.getT2(), equs);

			return new Let(tLet.getId(), ty, t1, t2);
		}
		else if (t instanceof If) {
			If tIf = (If) t;

			Term condTerm = substTerm(tIf.getCond(), equs);
			Term thenTerm = substTerm(tIf.getThenT(), equs);
			Term elseTerm = substTerm(tIf.getElseT(), equs);

			return new If(condTerm, thenTerm, elseTerm);
		}
		else if (t instanceof Arithmetic) {
			Arithmetic tArith = (Arithmetic) t;

			Term oprnd1 = substTerm(tArith.getOprnd1(), equs);

			if (tArith.getOprnd2() != null) {
				Term oprnd2 = substTerm(tArith.getOprnd2(), equs);

				return new Arithmetic(oprnd1, tArith.getOp(), oprnd2);
			}
			else
				return new Arithmetic(oprnd1, tArith.getOp());
		}
		else if (t instanceof Comp) {
			Comp tComp = (Comp) t;

			Term oprnd1 = substTerm(tComp.getOprnd1(), equs);
			Term oprnd2 = substTerm(tComp.getOprnd2(), equs);

			return new Comp(oprnd1, tComp.getOp(), oprnd2);
		}
		else if (t instanceof Logical) {
			Logical tLogic = (Logical) t;

			Term oprnd1 = substTerm(tLogic.getOprnd1(), equs);

			if (tLogic.getOprnd2() != null) {
				Term oprnd2 = substTerm(tLogic.getOprnd2(), equs);

				return new Logical(oprnd1, tLogic.getOp(), oprnd2);
			}
			else
				return new Logical(oprnd1, tLogic.getOp());
		}
		else
			return null;
	}

	public static Type substTyEqus(Type ty, Equations equs) {
		if (equs == null || equs.getEqus().isEmpty())
			return ty;
		else {
			ArrayList<Equ> equList = equs.getEqus();

			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;
					Type ty1 = equty.getTy1();

					if (ty1 instanceof VarType) {
						VarType varty = (VarType) ty1;
						ty = /* TypedRPCMain. */subst(ty, varty.getVar(), equty.getTy2());
					}
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					TypedLocation tyloc1 = equloc.getTyloc1();

					if (tyloc1 instanceof LocVarType) {
						LocVarType locvarty = (LocVarType) tyloc1;
						ty = /* TypedRPCMain. */substTyTyLoc(ty, locvarty.getVar(), equloc.getTyloc2());
					}
				}
			}
			return ty;
		}
	}

	public static TypedLocation substLocEqus(TypedLocation tyloc, Equations equs) {
		if (equs == null || equs.getEqus().isEmpty())
			return tyloc;
		else {
			ArrayList<Equ> equList = equs.getEqus();

			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					// do nothing
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					TypedLocation tyloc1 = equloc.getTyloc1();

					if (tyloc1 instanceof LocVarType) {
						LocVarType locvarty = (LocVarType) tyloc1;
						tyloc = /* TypedRPCMain. */substTyLoc(tyloc, locvarty.getVar(), equloc.getTyloc2());
					}
				}
			}

			return tyloc;
		}
	}

	public static Type subst(Type t, int i, Type ty) {
		check(i, ty);

		if (t instanceof IntType) {
			IntType intType = (IntType) t;

			return intType;
		}
		else if (t instanceof UnitType) {
			UnitType unitType = (UnitType) t;

			return unitType;
		}
		else if (t instanceof BoolType) {
			BoolType boolType = (BoolType) t;

			return boolType;
		}
		else if (t instanceof StrType) {
			StrType strType = (StrType) t;

			return strType;
		}
		else if (t instanceof VarType) {
			VarType varType = (VarType) t;

			if (i == varType.getVar())
				return ty;
			else
				return varType;
		}
		else if (t instanceof FunType) {
			FunType funType = (FunType) t;

			Type left = subst(funType.getFunTy(), i, ty);
			Type right = subst(funType.getArgTy(), i, ty);

			FunType retFunType = new FunType(left, funType.getLoc(), right);

			return retFunType;
		}
		else
			return null;
	}

	public static Type substTyTyLoc(Type t, int i, TypedLocation tyloc) {
		if (t instanceof IntType) {
			IntType intType = (IntType) t;

			return intType;
		}
		else if (t instanceof UnitType) {
			UnitType unitType = (UnitType) t;

			return unitType;
		}
		else if (t instanceof BoolType) {
			BoolType boolType = (BoolType) t;

			return boolType;
		}
		else if (t instanceof StrType) {
			StrType strType = (StrType) t;

			return strType;
		}
		else if (t instanceof VarType) {
			VarType varType = (VarType) t;

			return varType;
		}
		else if (t instanceof FunType) {
			FunType funType = (FunType) t;
			TypedLocation funTypedLocation = funType.getLoc();

			if (funTypedLocation instanceof LocType) {
				LocType locType = (LocType) funTypedLocation;

				return funType;
			}
			else if (funTypedLocation instanceof LocVarType) {
				LocVarType locVarType = (LocVarType) funTypedLocation;

				if (i == locVarType.getVar())
					return new FunType(funType.getFunTy(), tyloc, funType.getArgTy());
				else
					return funType;
			}
			else
				return null;
		}
		else
			return null;
	}

	public static TypedLocation substTyLoc(TypedLocation tyloc, int j, TypedLocation jtyloc) {
		if (tyloc instanceof LocVarType) {
			LocVarType locVarType = (LocVarType) tyloc;

			if (locVarType.getVar() == j)
				return jtyloc;
			else
				return locVarType;
		}
		else if (tyloc instanceof LocType) {
			LocType locType = (LocType) tyloc;

			return locType;
		}
		else
			return null;
	}

	public static void check(int i, Type ty) {
		if (ty instanceof IntType) {

		}
		else if (ty instanceof VarType) {
			VarType vTy = (VarType) ty;

			if (vTy.getVar() == i)
				throw new RuntimeException(i + " occurs in " + ty);
		}
		else if (ty instanceof FunType) {
			FunType fTy = (FunType) ty;

			check(i, fTy.getFunTy());
			check(i, fTy.getArgTy());
		}

	}
}
