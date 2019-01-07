package com.example.rpc.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.example.extrpc.App;
import com.example.extrpc.Bool;
import com.example.extrpc.BoolType;
import com.example.extrpc.ExprTerm;
import com.example.extrpc.FunType;
import com.example.extrpc.If;
import com.example.extrpc.Infer;
import com.example.extrpc.IntType;
import com.example.extrpc.Lam;
import com.example.extrpc.Let;
import com.example.extrpc.LocType;
import com.example.extrpc.LocVarType;
import com.example.extrpc.Location;
import com.example.extrpc.Num;
import com.example.extrpc.Parser;
import com.example.extrpc.Str;
import com.example.extrpc.StrType;
import com.example.extrpc.Term;
import com.example.extrpc.TopLevel;
import com.example.extrpc.Type;
import com.example.extrpc.TypedLocation;
import com.example.extrpc.Unit;
import com.example.extrpc.UnitType;
import com.example.extrpc.Var;
import com.example.extrpc.VarType;
import com.example.lib.LexerException;
import com.example.lib.ParserException;

public class InferRegressionTest {
	private Parser parser;

	@Test
	public void test() throws IOException, LexerException, ParserException {
		parser = new Parser();
		String directory = System.getProperty("user.dir");
		recursiveRead(new File(directory + "/testcase/"));

	}

	public void recursiveRead(File folder) throws ParserException, IOException, LexerException {
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile() && file.toPath().toString().contains(".txt")) {
//				if (file.toPath().toString().contains("total"))
//					continue;
				System.out.println("\n" + file);
				FileReader fileReader = new FileReader(file);
				Term ex1 = parser.Parsing(fileReader);
				Term tyEx1 = Infer.infer(ex1);
				prettyPrint(tyEx1);
			}
			else if (file.isDirectory()) {
				if (!(file.toPath().toString().contains("test_result") || file.toPath().toString().contains("examples"))) {
					recursiveRead(new File(file + "/"));
				}
			}
		}
	}

	public void prettyPrint(Term t) {
		if (t instanceof App)
			prettyPrint((App) t);
		else if (t instanceof Bool)
			prettyPrint((Bool) t);
		else if (t instanceof ExprTerm)
			prettyPrint((ExprTerm) t);
		else if (t instanceof If)
			prettyPrint((If) t);
		else if (t instanceof Lam)
			prettyPrint((Lam) t);
		else if (t instanceof Let)
			prettyPrint((Let) t);
		else if (t instanceof Num)
			prettyPrint((Num) t);
		else if (t instanceof Str)
			prettyPrint((Str) t);
		else if (t instanceof TopLevel)
			prettyPrint((TopLevel) t);
		else if (t instanceof Unit)
			prettyPrint((Unit) t);
		else if (t instanceof Var)
			prettyPrint((Var) t);
	}

	private int indent = 0;

	public void printIndent() {
		for (int i = 0; i < indent; i++) {
			System.out.print("    ");
		}
	}

	public void prettyPrint(App app) {
		System.out.print("(");
		prettyPrint(app.getFun());
		System.out.print(")^");
		prettyPrintLoc(app.getLoc());
		System.out.print("(");
		prettyPrint(app.getArg());
		System.out.print(")");
	}


	public void prettyPrint(Bool bool) {
		System.out.print(bool.isBool());
	}
	
	public void prettyPrint(ExprTerm exprTerm) {
		if (exprTerm.getOprnd2() == null) {
			System.out.print(exprTerm.get(exprTerm.getOp()));
			prettyPrint(exprTerm.getOprnd1());
		}
		else {
			prettyPrint(exprTerm.getOprnd1());
			System.out.print(" " + exprTerm.get(exprTerm.getOp()) + " ");
			prettyPrint(exprTerm.getOprnd2());
		}
	}

	public void prettyPrint(If ifTerm) {
		System.out.print("if ");
		prettyPrint(ifTerm.getCond());
		System.out.print(" then\n");
		indent++;
		printIndent();
		prettyPrint(ifTerm.getThenT());
		System.out.print("\n");
		indent--;
		printIndent();
		System.out.print("else\n");
		indent++;
		printIndent();
		prettyPrint(ifTerm.getElseT());
		indent--;
	}

	public void prettyPrint(Lam lam) {
		System.out.print("(lam");
		if (lam.getLoc() == Location.Client)
			System.out.print("^c ");
		else
			System.out.print("^s ");
		System.out.print("(" + lam.getX() + ": ");
		prettyPrintType(lam.getIdTy());
		System.out.print(").\n");
		indent++;
		printIndent();
		prettyPrint(lam.getM());
		indent--;
		System.out.print(")");
	}

	public void prettyPrint(Let let) {
		System.out.print("let " + let.getId());
		System.out.print(": ");
		prettyPrintType(let.getIdTy());
		System.out.print(" = \n");
		indent++;
		printIndent();
		prettyPrint(let.getT1());
		System.out.print(" in\n");
		indent++;
		printIndent();
		prettyPrint(let.getT2());
		indent--;
		indent--;
		System.out.println();
		printIndent();
		System.out.print("end\n");
	}

	public void prettyPrint(Num num) {
		System.out.print(num.getI());
	}

	public void prettyPrint(Str str) {
		System.out.print(str.getStr());
	}

	public void prettyPrint(TopLevel topLevel) {
		prettyPrint(topLevel.getTerm());
		if (topLevel.getNext() != null) {
			System.out.println(";");
			prettyPrint(topLevel.getNext());
		}
	}

	public void prettyPrint(Unit unit) {
		System.out.print("()");
	}

	public void prettyPrint(Var var) {
		System.out.print(var.getVar());
	}
	
	public void prettyPrintType(Type ty) {
		if (ty instanceof UnitType) {
			System.out.print("Unit");
		}
		else if (ty instanceof BoolType) {
			System.out.print("bool");
		}
		else if (ty instanceof IntType) {
			System.out.print("int");
		}
		else if (ty instanceof StrType) {
			System.out.print("string");
		}
		else if (ty instanceof VarType) {
			VarType varTy = (VarType) ty;
			
			System.out.print("a" + varTy.getVar());
		}
		else if (ty instanceof FunType) {
			FunType funTy = (FunType) ty;
			
			prettyPrintType(funTy.getArgTy());
			System.out.print("-");
			prettyPrintLoc(funTy.getLoc());
			System.out.print("->");
			prettyPrintType(funTy.getRetTy());
		}
	}
	
	public void prettyPrintLoc(TypedLocation tyloc) {
		if (tyloc instanceof LocType) {
			LocType locTy = (LocType) tyloc;
			
			System.out.print(locTy.getLoc());
		}
		else if (tyloc instanceof LocVarType) {
			LocVarType locVarTy = (LocVarType) tyloc;
			
			System.out.print("l" + locVarTy.getVar());
		}
	}
}
