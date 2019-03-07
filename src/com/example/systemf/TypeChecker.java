package com.example.systemf;

import java.util.ArrayList;

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
import com.example.systemf.ast.Location;
import com.example.systemf.ast.Num;
import com.example.systemf.ast.PrimTerm;
import com.example.systemf.ast.Str;
import com.example.systemf.ast.StrType;
import com.example.systemf.ast.Tapp;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.Tylam;
import com.example.systemf.ast.Type;
import com.example.systemf.ast.TypedLocation;
import com.example.systemf.ast.Unit;
import com.example.systemf.ast.UnitType;
import com.example.systemf.ast.Var;
import com.example.systemf.ast.VarType;

import javafx.util.Pair;

public class TypeChecker {	
	public static Type check(Term term, TyEnv tyenv) throws TypeCheckException {
		initLibrary(tyenv);

		Type termTy = checkTerm(term, tyenv, Location.Client);

		return termTy;
	}
	
	private static void initLibrary(TyEnv env) {
		generalLibrary(env);
		dbLibrary(env);
	}
	
	private static void generalLibrary(TyEnv env) {
		ArrayList<Pair<String, Type>> envList = new ArrayList<>();
		TypedLocation client = new LocType(Location.Client);
		TypedLocation server = new LocType(Location.Server);
		
		// isNothing, fromJust -> client, server
		envList.add(new Pair<>("isNothing_client", new FunType(new StrType(), client, new BoolType())));
		envList.add(new Pair<>("isNothing_server", new FunType(new StrType(), server, new BoolType())));
		envList.add(new Pair<>("fromJust_client", new FunType(new StrType(), client, new StrType())));
		envList.add(new Pair<>("fromJust_server", new FunType(new StrType(), server, new StrType())));
		
		envList.add(new Pair<>("openFile_client", new FunType(new StrType(), client, new FunType(new StrType(), client, new StrType()))));
		envList.add(new Pair<>("openFile_server", new FunType(new StrType(), client, new FunType(new StrType(), client, new StrType()))));
		envList.add(new Pair<>("closeFile_client", new FunType(new StrType(), client, new UnitType())));
		envList.add(new Pair<>("closeFile_server", new FunType(new StrType(), server, new UnitType())));
		envList.add(new Pair<>("writeFile_client", new FunType(new StrType(), client, new FunType(new StrType(), client, new StrType()))));
		envList.add(new Pair<>("writeFile_server", new FunType(new StrType(), client, new FunType(new StrType(), client, new StrType()))));
		envList.add(new Pair<>("readFile_client", new FunType(new StrType(), client, new StrType())));
		envList.add(new Pair<>("readFile_server", new FunType(new StrType(), server, new StrType())));
		
		envList.add(new Pair<>("readConsole", new FunType(new UnitType(), client, new StrType())));
		envList.add(new Pair<>("writeConsole", new FunType(new StrType(), client, new UnitType())));
		
		envList.add(new Pair<>("primReadConsole", new FunType(new UnitType(), client, new StrType())));
		envList.add(new Pair<>("primWriteConsole", new FunType(new StrType(), client, new UnitType())));
		
		envList.add(new Pair<>("toString_client", new ForAll(new VarType("toStringTy_client"), new FunType(new VarType("toStringTy_client"), client, new StrType()))));
		envList.add(new Pair<>("toString_server", new ForAll(new VarType("toStringTy_server"), new FunType(new VarType("toStringTy_server"), server, new StrType()))));
		envList.add(new Pair<>("toInt_client", new FunType(new StrType(), client, new IntType())));
		envList.add(new Pair<>("toInt_server", new FunType(new StrType(), server, new IntType())));
		envList.add(new Pair<>("toBool_client", new FunType(new StrType(), client, new BoolType())));
		envList.add(new Pair<>("toBool_server", new FunType(new StrType(), server, new BoolType())));
		
		envList.add(new Pair<>("reverse_client", new FunType(new StrType(), client, new StrType())));
		envList.add(new Pair<>("reverse_server", new FunType(new StrType(), server, new StrType())));
		envList.add(new Pair<>("append_client", new FunType(new StrType(), client, new FunType(new StrType(), client, new StrType()))));
		envList.add(new Pair<>("append_server", new FunType(new StrType(), server, new FunType(new StrType(), server, new StrType()))));
		envList.add(new Pair<>("length_client", new FunType(new StrType(), client, new IntType())));
		envList.add(new Pair<>("length_server", new FunType(new StrType(), server, new IntType())));
		
		envList.add(new Pair<>("getHour_client", new FunType(new UnitType(), client, new IntType())));
		envList.add(new Pair<>("getHour_server", new FunType(new UnitType(), server, new IntType())));
		envList.add(new Pair<>("getYear_client", new FunType(new UnitType(), client, new IntType())));
		envList.add(new Pair<>("getYear_server", new FunType(new UnitType(), server, new IntType())));
		envList.add(new Pair<>("getMonth_client", new FunType(new UnitType(), client, new IntType())));
		envList.add(new Pair<>("getMonth_server", new FunType(new UnitType(), server, new IntType())));
		envList.add(new Pair<>("getDay_client", new FunType(new UnitType(), client, new IntType())));
		envList.add(new Pair<>("getDay_server", new FunType(new UnitType(), server, new IntType())));
		envList.add(new Pair<>("getDate_client", new FunType(new UnitType(), client, new IntType())));
		envList.add(new Pair<>("getDate_server", new FunType(new UnitType(), server, new IntType())));
		
		env.getPairList().addAll(envList);
	}
	
	private static void dbLibrary(TyEnv env) {
		ArrayList<Pair<String, Type>> envList = new ArrayList<>();
		TypedLocation server = new LocType(Location.Server);
		
		envList.add(new Pair<>("createTable", new FunType(new StrType(), server, new FunType(new StrType(), server, new IntType()))));
		envList.add(new Pair<>("insertRecord", new FunType(new StrType(), server, new FunType(new StrType(), server, new BoolType()))));
		envList.add(new Pair<>("updateRecord", new FunType(new StrType(), server, new FunType(new StrType(), server, new BoolType()))));
		envList.add(new Pair<>("deleteRecord", new FunType(new StrType(), server, new FunType(new StrType(), server, new BoolType()))));
		envList.add(new Pair<>("query", new FunType(new StrType(), server, new FunType(new IntType(), server, new FunType(new StrType(), server, new StrType())))));
		envList.add(new Pair<>("fromRecord", new FunType(new StrType(), server, new FunType(new IntType(), server, new StrType()))));
		
		env.getPairList().addAll(envList);
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
		else if (t instanceof Tylam) {
			// tylam id. ty
			Tylam tylam = (Tylam) t;
			
			Term term = tylam.getTerm();
			
			assert (tylam.getTy() instanceof VarType);
			
			ArrayList<Pair<String, Type>> envList = tyenv.getPairList();

			VarType tylamId = (VarType) tylam.getTy();
			
			for (Pair<String, Type> p: envList) {
				if (p.getValue().toString().equals(tylamId.getVar()))
					throw new TypeCheckException("Type Environment contains " + tylam.getTy());
			}
			
			Pair<String, Type> tmpPair = new Pair<>(tylamId.getVar(), tylam.getTy());
			tyenv.getPairList().add(tmpPair);
			
			Type termTy = checkTerm(term, tyenv, loc);
			
			tyenv.getPairList().remove(tmpPair);
			
			return new ForAll(tylam.getTy(), termTy);
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
		else if (t instanceof Tapp) {
			Tapp tTApp = (Tapp) t;
			
			Type tfunTy = tTApp.getTy();
			
			Term tFun = tTApp.getFun();
			Type tFunType = checkTerm(tFun, tyenv, loc);
			
			if (tFunType instanceof ForAll) {
				ForAll forAll = (ForAll) tFunType;
				
				Type tyId = forAll.getTyId();
				
				if (tyId instanceof VarType) {
					VarType var = (VarType) tyId;
					Type substForAll = subst(forAll.getTy(), var.getVar(), tfunTy);
					
					return substForAll;
				}
				else
					throw new TypeCheckException("ForAll type id is not VarType(" + tyId + ")");
			}
			else
				throw new TypeCheckException("Function(" + tFun + ": " + tFunType + ") is not ForAll Type");
		}
		else if (t instanceof Let) {
			Let tLet = (Let) t;
			
			Term t1 = tLet.getT1();
			Term t2 = tLet.getT2();
			Pair<String, Type> pair = new Pair<>(tLet.getId(), tLet.getIdTy());

			tyenv.getPairList().add(pair);
			Type t1Ty = checkTerm(t1, tyenv, loc);
			Type t2Ty = checkTerm(t2, tyenv, loc);
			
			if (t1Ty.toString().equals(tLet.getIdTy().toString()))
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
		else if (t instanceof PrimTerm) {
			PrimTerm tExpr = (PrimTerm) t;
			int op = tExpr.getOp();
			
			if (op >= 0 && op <= 3) {
				Term oprnd1 = tExpr.getOprnds().get(0);
				Term oprnd2 = tExpr.getOprnds().get(1);
				
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				Type oprnd2Ty = checkTerm(oprnd2, tyenv, loc);
				
				if (oprnd1Ty instanceof IntType && oprnd2Ty instanceof IntType) {
					return oprnd1Ty;
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty + ", Oprnd2(" + oprnd2 + ") type is " + oprnd2Ty);
			}
			else if (op == 4) {
				Term oprnd1 = tExpr.getOprnds().get(0);
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				
				if (oprnd1Ty instanceof IntType) {
					return oprnd1Ty;
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty);
			}
			else if (op >= 5 && op <= 8) {
				Term oprnd1 = tExpr.getOprnds().get(0);
				Term oprnd2 = tExpr.getOprnds().get(1);
				
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				Type oprnd2Ty = checkTerm(oprnd2, tyenv, loc);
				
				if (oprnd1Ty instanceof IntType && oprnd2Ty instanceof IntType) {
					return new BoolType();
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty + ", Oprnd2(" + oprnd2 + ") type is " + oprnd2Ty);
			}
			else if (op >= 9 && op <= 10) {
				Term oprnd1 = tExpr.getOprnds().get(0);
				Term oprnd2 = tExpr.getOprnds().get(1);
				
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				Type oprnd2Ty = checkTerm(oprnd2, tyenv, loc);
				
				if (oprnd1Ty.getClass() == oprnd2Ty.getClass())
					return new BoolType();
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty + ", Oprnd2(" + oprnd2 + ") type is " + oprnd2Ty);
			}
			else if (op >= 11 && op <= 12) {
				Term oprnd1 = tExpr.getOprnds().get(0);
				Term oprnd2 = tExpr.getOprnds().get(1);
				
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				Type oprnd2Ty = checkTerm(oprnd2, tyenv, loc);
				
				if (oprnd1Ty instanceof BoolType && oprnd2Ty instanceof BoolType) {
					return oprnd1Ty;
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty + ", Oprnd2(" + oprnd2 + ") type is " + oprnd2Ty);
			}
			else {
				Term oprnd1 = tExpr.getOprnds().get(0);
				Type oprnd1Ty = checkTerm(oprnd1, tyenv, loc);
				
				if (oprnd1Ty instanceof BoolType) {
					return oprnd1Ty;
				}
				else
					throw new TypeCheckException("Oprnd1(" + oprnd1 + ") type is " + oprnd1Ty);
			}
		}
		else if (t instanceof LibTerm) {
			LibTerm tLibTerm = (LibTerm) t;
			
			String funName = tLibTerm.getFunName();
			ArrayList<String> args = tLibTerm.getArgs();
			
			int idx = 0;
			
			Type tyFunName = tylookup(funName, tyenv);
			
			while (tyFunName instanceof FunType) {
				FunType funTy = (FunType) tyFunName;
				Type funTyArg = funTy.getArgTy();
				Type funTyRet = funTy.getRetTy();
				
				Type tyArg = tylookup(args.get(idx), tyenv); 
				
				if (!funTyArg.equals(tyArg)) {
					throw new TypeCheckException("LibTerm(" + tLibTerm +")");
				}
				
				tyFunName = funTyRet;
			}
			
			return tyFunName;
		}
		else
			throw new TypeCheckException("Not Expected Term: " + t);
		
		return null;
	}
	public static Type subst(Type t, String s, Type ty) {
		check(s, ty);

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

			if (s.equals(varType.getVar()))
				return ty;
			else
				return varType;
		}
		else if (t instanceof FunType) {
			FunType funType = (FunType) t;
			Type left = subst(funType.getArgTy(), s, ty);
			Type right = subst(funType.getRetTy(), s, ty);

			FunType retFunType = new FunType(left, funType.getLoc(), right);

			return retFunType;
		}
		else if (t instanceof ForAll) {
			ForAll forAllType = (ForAll) t;
			
			if (forAllType.getTyId().equals(s)) {
				return forAllType;
			}
			else {
				Type forAllTy = subst(forAllType.getTy(), s, ty);
				
				return new ForAll(forAllType.getTyId(), forAllTy);
			}
		}
		else {
			assert false;
			return null;
		}
	}
	
	public static void check(String s, Type ty) {
		if (ty instanceof IntType) {

		}
		else if (ty instanceof VarType) {
			VarType vTy = (VarType) ty;

			if (vTy.getVar().equals(s))
				throw new RuntimeException(s + " occurs in " + ty);
		}
		else if (ty instanceof FunType) {
			FunType fTy = (FunType) ty;

			check(s, fTy.getArgTy());
			check(s, fTy.getRetTy());
		}

	}
}
