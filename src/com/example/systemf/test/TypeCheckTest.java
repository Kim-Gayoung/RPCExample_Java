package com.example.systemf.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.systemf.TyEnv;
import com.example.systemf.TypeCheckException;
import com.example.systemf.TypeChecker;
import com.example.systemf.ast.App;
import com.example.systemf.ast.Bool;
import com.example.systemf.ast.BoolType;
import com.example.systemf.ast.ExprTerm;
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
import com.example.systemf.ast.Tapp;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.Tylam;
import com.example.systemf.ast.Type;
import com.example.systemf.ast.TypedLocation;
import com.example.systemf.ast.Unit;
import com.example.systemf.ast.UnitType;
import com.example.systemf.ast.Var;
import com.example.systemf.ast.VarType;
import com.example.systemf.parser.Parser;

public class TypeCheckTest {
	Parser parser;

	@Test
	public void test() throws IOException, ParserException, LexerException, TypeCheckException {
		parser = new Parser();
		String directory = System.getProperty("user.dir");
		recursiveRead(new File(directory + "/testcase/examples/systemF/"));
	}

	public void recursiveRead(File file) throws ParserException, IOException, LexerException, TypeCheckException {
		File[] listOfFiles = file.listFiles();

		for (File f : listOfFiles) {
			if (f.isFile()) {
				System.out.println("\n" + f);
				FileReader fileReader = new FileReader(f);
				Term ex1 = (Term) parser.Parsing(fileReader);
				Type ty1 = TypeChecker.check(ex1, new TyEnv());
				prettyPrint(ex1);
				System.out.println("Type: " + ty1.toString());
			}
		}
	}
	
	@Test
	public void simpleCase() throws ParserException, IOException, LexerException, TypeCheckException {
		parser = new Parser();
		String directory = System.getProperty("user.dir");
		File file = new File(directory + "/testcase/examples/systemF/simple/");
		File[] listOfFiles = file.listFiles();

		for (File f : listOfFiles) {
			if (f.isFile()) {
				System.out.println("\n" + f);
				FileReader fileReader = new FileReader(f);
				Term ex1 = (Term) parser.Parsing(fileReader);
				Type ty1 = TypeChecker.check(ex1, new TyEnv());
				prettyPrint(ex1);
				System.out.println("Type: " + ty1.toString());
			}
		}
	}

	@Test(expected = TypeCheckException.class)
	public void duplicateTypeVarFail() throws ParserException, IOException, LexerException, TypeCheckException {
		parser = new Parser();
		String directory = System.getProperty("user.dir");
		File f = new File(directory + "/testcase/examples/systemF/failure/example_duplicate_typevar_fail.txt");

		System.out.println("\n" + f);
		FileReader fileReader = new FileReader(f);
		Term ex1 = (Term) parser.Parsing(fileReader);
		Type ty1 = TypeChecker.check(ex1, new TyEnv());
	}

	public void prettyPrint(Term t) {
		if (t instanceof App)
			prettyPrint((App) t);
		else if (t instanceof Tapp)
			prettyPrint((Tapp) t);
		else if (t instanceof Bool)
			prettyPrint((Bool) t);
		else if (t instanceof ExprTerm)
			prettyPrint((ExprTerm) t);
		else if (t instanceof If)
			prettyPrint((If) t);
		else if (t instanceof Lam)
			prettyPrint((Lam) t);
		else if (t instanceof Tylam)
			prettyPrint((Tylam) t);
		else if (t instanceof Let)
			prettyPrint((Let) t);
		else if (t instanceof Num)
			prettyPrint((Num) t);
		else if (t instanceof Str)
			prettyPrint((Str) t);
		else if (t instanceof Unit)
			prettyPrint((Unit) t);
		else if (t instanceof Var)
			prettyPrint((Var) t);
	}

	private int indent = 0;

	public void printIndent() {
		for (int i = 0; i < indent; i++) {
			System.out.print("   ");
		}
	}

	public void prettyPrint(App app) {
		System.out.print("(");
		prettyPrint(app.getFun());
		System.out.print(") ^");
		prettyPrintLoc(app.getLoc());
		System.out.print("^ (");
		prettyPrint(app.getArg());
		System.out.print(")");
	}

	public void prettyPrint(Tapp tapp) {
		System.out.print("(");
		prettyPrint(tapp.getFun());
		System.out.print("[");
		prettyPrintType(tapp.getTy());
		System.out.print("])");
	}

	public void prettyPrint(Bool bool) {
		System.out.print(bool.isBool());
	}

	public void prettyPrint(ExprTerm exprTerm) {
		if (exprTerm.getOprnds().size() == 1) {
			System.out.print(exprTerm.get(exprTerm.getOp()));
			prettyPrint(exprTerm.getOprnds().get(0));
		}
		else {
			prettyPrint(exprTerm.getOprnds().get(0));
			System.out.print(" " + exprTerm.get(exprTerm.getOp()) + " ");
			prettyPrint(exprTerm.getOprnds().get(1));
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
		System.out.print("lam");
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
	}

	public void prettyPrint(Tylam tylam) {
		System.out.print("tylam ");
		prettyPrintType(tylam.getTy());
		System.out.print(".");
		prettyPrint(tylam.getTerm());
	}

	public void prettyPrint(Let let) {
		System.out.print("let " + let.getId());
		System.out.print(": ");
		prettyPrintType(let.getIdTy());
		System.out.print(" = ");
		prettyPrint(let.getT1());
		System.out.println();
		printIndent();
		System.out.print("in\n");
		indent++;
		printIndent();
		prettyPrint(let.getT2());
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
