package com.example.systemf;

import java.util.HashSet;
import java.util.Set;

import com.example.systemf.ast.All;
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
import com.example.systemf.ast.Location;
import com.example.systemf.ast.Num;
import com.example.systemf.ast.Str;
import com.example.systemf.ast.StrType;
import com.example.systemf.ast.TApp;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.TopLevel;
import com.example.systemf.ast.Type;
import com.example.systemf.ast.TypedLocation;
import com.example.systemf.ast.Unit;
import com.example.systemf.ast.UnitType;
import com.example.systemf.ast.Var;
import com.example.systemf.ast.VarType;

import javafx.util.Pair;

public class TypeChecker {	
	public static Type checkTopLevel(TopLevel top, TyEnv tyenv) throws TypeCheckException {
		Term term = top.getTop();
		Type termTy = checkTerm(term, tyenv, Location.Client);
		
		if (top.getNext() != null) {
			TopLevel next = top.getNext();
			Type nextTy = checkTopLevel(next, tyenv);
			
			return nextTy;
		}
		else {
			return termTy;
		}
	}
	
	public static Type tylookup(String x, TyEnv tyenv) {
		for (Pair<String, Type> p: tyenv.getPairList()) {
			if (p.getKey().equals(x))
				return p.getValue();
		}
		
		return null;
	}
	
	public static Type checkTerm(Term t, TyEnv tyenv, Location loc) throws TypeCheckException {
		if (t instanceof Unit) {
			return new UnitType();
		}
		else if (t instanceof Num) {
			return new IntType();
		}
		else if (t instanceof Str) {
			return new StrType();
		}
		else if (t instanceof Bool) {
			return new BoolType();
		}
		else if (t instanceof Var) {
			Var tVar = (Var) t;
			Type varTy = tylookup(tVar.getVar(), tyenv);
			
			return varTy;
		}
		else if (t instanceof Lam) {
			Lam tLam = (Lam) t;
			
			Type varTy = tLam.getIdTy();
			Pair<String, Type> pair = new Pair<>(tLam.getX(), varTy);
			tyenv.getPairList().add(pair);
			Location lamLoc = tLam.getLoc();
			
			Type termTy = checkTerm(tLam.getM(), tyenv, lamLoc);
			tyenv.getPairList().remove(pair);
			
			return new FunType(varTy, new LocType(lamLoc), termTy);
		}
		else if (t instanceof All) {
			All tAll = (All) t;
			
			Term term = tAll.getTerm();
			Type termTy = checkTerm(term, tyenv, loc);
			
			Set<Integer> tyInts = new HashSet<>();
			
			if (tAll.getTy() instanceof VarType) {
				VarType varTy = (VarType) tAll.getTy();
				tyInts.add(varTy.getVar());
			}
			
			return new ForAll(new HashSet<>(), tyInts, termTy);
		}
		else if (t instanceof App) {
			App tApp = (App) t;
			
			Type funTy = checkTerm(tApp.getFun(), tyenv, loc);
			Type argTy = null;
			
			if (funTy instanceof FunType) {
				FunType funType = (FunType) funTy;
				
				Type argType = funType.getArgTy();
				Type retType = funType.getRetTy();
				TypedLocation typedLoc = funType.getLoc();
				
				boolean flag = false;
				
				if (typedLoc instanceof LocType) {
					LocType locType = (LocType) typedLoc;
					Location location = locType.getLoc();
					
					if (location == loc) {
						argTy = checkTerm(tApp.getArg(), tyenv, loc);
					}
					else if (location == Location.Server && loc == Location.Client) {

						argTy = checkTerm(tApp.getArg(), tyenv, Location.Client);
					}
					else {
						argTy = checkTerm(tApp.getArg(), tyenv, Location.Server);
					}
					
					if (argType.getClass() != argTy.getClass())
						flag = true;
				}
				else
					flag = true;

				if (!flag)
					return retType;
				else {
					String msg = tApp + ":\nFunType is " + funTy + "\n";
					msg += "ArgType is " + argTy + "\n";
					msg += "Location is " + typedLoc + "\n";
					msg += "Current Location is " + loc + ".";
					
					throw new TypeCheckException(msg);
				}
			}
			else
				throw new TypeCheckException("Function(" + tApp.getFun() + ": " + funTy +") is not FunType.");
		}
		else if (t instanceof TApp) {
			TApp tTApp = (TApp) t;
			
			Type funTy = tTApp.getTy();
			
			Term fun = tTApp.getFun();
			Type funType = checkTerm(fun, tyenv, loc);
			
			if (funType instanceof ForAll) {
				ForAll forallTy = (ForAll) funType;
				Type ty = forallTy.getTy();
				
				if (ty instanceof VarType) {
					VarType varTy = (VarType) ty;
					int i = varTy.getVar();
					
					Type substTy = subst(forallTy.getTy(), i, funTy);
					
					return substTy;
				}
				else
					throw new TypeCheckException("ForAll(" + ty + ") is not VarType.");
			}
			else
				throw new TypeCheckException("Function(" + fun + ": " + funType + ") is not ForAll Type");
		}
		else if (t instanceof Let) {
			Let tLet = (Let) t;
			
			Term t1 = tLet.getT1();
			Term t2 = tLet.getT2();
			Pair<String, Type> pair = new Pair<>(tLet.getId(), tLet.getIdTy());
			
			Type t1Ty = checkTerm(t1, tyenv, loc);
			tyenv.getPairList().add(pair);
			Type t2Ty = checkTerm(t2, tyenv, loc);
			
			if (t1Ty.equals(tLet.getIdTy()))
				return t2Ty;
			else
				throw new TypeCheckException("T1(" + t1 + ": " + t1Ty + ") is not equal ID type(" + tLet.getId() + ": " + tLet.getIdTy() + ")");
		}
		else if (t instanceof If) {
			If tIf = (If) t;
			
			Type condTy = checkTerm(tIf.getCond(), tyenv, loc);
			Type thenTy = checkTerm(tIf.getThenT(), tyenv, loc);
			Type elseTy = checkTerm(tIf.getElseT(), tyenv, loc);
			
			if (condTy instanceof BoolType) {
				if (thenTy.getClass() == elseTy.getClass())
					return thenTy;
			}
			else
				throw new TypeCheckException(condTy + " is not BoolType.");
		}
		else if (t instanceof ExprTerm) {
			ExprTerm tExpr = (ExprTerm) t;
			int op = tExpr.getOp();
			
			if (op >= 0 && op <= 3) {
				Term oprnd1 = tExpr.getOprnd1();
				Term oprnd2 = tExpr.getOprnd2();
				
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				Type oprnd2Ty = checkTerm(oprnd2, tyenv, loc);
				
				if (oprnd1Ty instanceof IntType && oprnd2Ty instanceof IntType) {
					return oprnd1Ty;
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty + ", Oprnd2(" + oprnd2 + ") type is " + oprnd2Ty);
			}
			else if (op == 4) {
				Term oprnd1 = tExpr.getOprnd1();
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				
				if (oprnd1Ty instanceof IntType) {
					return oprnd1Ty;
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty);
			}
			else if (op >= 5 && op <= 8) {
				Term oprnd1 = tExpr.getOprnd1();
				Term oprnd2 = tExpr.getOprnd2();
				
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				Type oprnd2Ty = checkTerm(oprnd2, tyenv, loc);
				
				if (oprnd1Ty instanceof IntType && oprnd2Ty instanceof IntType) {
					return new BoolType();
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty + ", Oprnd2(" + oprnd2 + ") type is " + oprnd2Ty);
			}
			else if (op >= 9 && op <= 10) {
				Term oprnd1 = tExpr.getOprnd1();
				Term oprnd2 = tExpr.getOprnd2();
				
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				Type oprnd2Ty = checkTerm(oprnd2, tyenv, loc);
				
				if (oprnd1Ty.getClass() == oprnd2Ty.getClass())
					return new BoolType();
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty + ", Oprnd2(" + oprnd2 + ") type is " + oprnd2Ty);
			}
			else if (op >= 11 && op <= 12) {
				Term oprnd1 = tExpr.getOprnd1();
				Term oprnd2 = tExpr.getOprnd2();
				
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				Type oprnd2Ty = checkTerm(oprnd2, tyenv, loc);
				
				if (oprnd1Ty instanceof BoolType && oprnd2Ty instanceof BoolType) {
					return oprnd1Ty;
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty + ", Oprnd2(" + oprnd2 + ") type is " + oprnd2Ty);
			}
			else {
				Term oprnd1 = tExpr.getOprnd1();
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				
				if (oprnd1Ty instanceof BoolType) {
					return oprnd1Ty;
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty);
			}
		}
		else
			throw new TypeCheckException("Not Expected Term: " + t);
		
		return null;
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
			Type left = subst(funType.getArgTy(), i, ty);
			Type right = subst(funType.getRetTy(), i, ty);

			FunType retFunType = new FunType(left, funType.getLoc(), right);

			return retFunType;
		}
		else if (t instanceof ForAll) {
			ForAll forAllType = (ForAll) t;
			
			if (forAllType.getTyInts().contains(i)) {
				return forAllType;
			}
			else {
				Type forAllTy = subst(forAllType.getTy(), i, ty);
				
				return new ForAll(forAllType.getLocInts(), forAllType.getTyInts(), forAllTy);
			}
		}
		else {
			assert false;
			return null;
		}
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

			check(i, fTy.getArgTy());
			check(i, fTy.getRetTy());
		}

	}
}
