package com.example.rpc.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.example.extrpc.App;
import com.example.extrpc.Bool;
import com.example.extrpc.ExprTerm;
import com.example.extrpc.If;
import com.example.extrpc.Lam;
import com.example.extrpc.Let;
import com.example.extrpc.Location;
import com.example.extrpc.Num;
import com.example.extrpc.Parser;
import com.example.extrpc.Str;
import com.example.extrpc.Term;
import com.example.extrpc.TopLevel;
import com.example.extrpc.Unit;
import com.example.extrpc.Var;
import com.example.lib.LexerException;
import com.example.lib.ParserException;

public class ParseRegressionTest {
	private Parser parser;

	@Test
	public void test() throws IOException, LexerException, ParserException {
		parser = new Parser();
		String directory = System.getProperty("user.dir");
		recursiveRead(new File(directory + "/testcase/"));

	}

	public void recursiveRead(File folder) throws ParserException, IOException, LexerException {
		File[] listOfFiles = folder.listFiles();
		// System.out.println(listOfFiles[0].toPath());

		for (File file : listOfFiles) {
			if (file.isFile() && file.toPath().toString().contains(".txt")) {
				System.out.println("\n" + file);
				FileReader fileReader = new FileReader(file);
				Term ex1 = parser.Parsing(fileReader);
				prettyPrint(ex1);
			}
			else if (file.isDirectory()) {
				if (!file.toPath().toString().contains("test_result")) {
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
		prettyPrint(app.getFun());
		System.out.print(" ");
		prettyPrint(app.getArg());
	}

	public void prettyPrint(Bool bool) {
		System.out.print(bool.isBool());
	}

	public void prettyPrint(ExprTerm exprTerm) {
		if (exprTerm.getOprnd2() == null) {
			System.out.print(exprTerm.getOp());
			prettyPrint(exprTerm.getOprnd1());
		}
		else {
			prettyPrint(exprTerm.getOprnd1());
			System.out.print(" " + exprTerm.getOp() + " ");
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
		System.out.print("lam");
		if (lam.getLoc() == Location.Client)
			System.out.print("^c ");
		else
			System.out.print("^s ");
		System.out.print("(" + lam.getX() + ")");
		System.out.print(".\n");
		indent++;
		printIndent();
		prettyPrint(lam.getM());
		indent--;
		System.out.println();
	}

	public void prettyPrint(Let let) {
		System.out.print("let " + let.getId());
		System.out.print(" = ");
		prettyPrint(let.getT1());
		System.out.print(" in\n");
		indent++;
		printIndent();
		prettyPrint(let.getT2());
		indent--;
		System.out.println();
		printIndent();
		System.out.print("end");
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

}
