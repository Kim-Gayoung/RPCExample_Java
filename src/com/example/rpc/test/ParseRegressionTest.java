package com.example.rpc.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.rpc.App;
import com.example.rpc.Arithmetic;
import com.example.rpc.Bool;
import com.example.rpc.Comp;
import com.example.rpc.If;
import com.example.rpc.Lam;
import com.example.rpc.Let;
import com.example.rpc.Location;
import com.example.rpc.Logical;
import com.example.rpc.Num;
import com.example.rpc.Params;
import com.example.rpc.Parser;
import com.example.rpc.Str;
import com.example.rpc.Term;
import com.example.rpc.TopLevel;
import com.example.rpc.Unit;
import com.example.rpc.Var;

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
		else if (t instanceof Arithmetic)
			prettyPrint((Arithmetic) t);
		else if (t instanceof Bool)
			prettyPrint((Bool) t);
		else if (t instanceof Comp)
			prettyPrint((Comp) t);
		else if (t instanceof If)
			prettyPrint((If) t);
		else if (t instanceof Lam)
			prettyPrint((Lam) t);
		else if (t instanceof Let)
			prettyPrint((Let) t);
		else if (t instanceof Logical)
			prettyPrint((Logical) t);
		else if (t instanceof Num)
			prettyPrint((Num) t);
		else if (t instanceof Params)
			prettyPrint((Params) t);
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

	public void prettyPrint(Arithmetic arith) {
		if (arith.getOprnd2() == null) {
			System.out.print(arith.getOp());
			prettyPrint(arith.getOprnd1());
		}
		else {
			prettyPrint(arith.getOprnd1());
			System.out.print(" " + arith.getOp() + " ");
			prettyPrint(arith.getOprnd2());
		}
	}

	public void prettyPrint(Bool bool) {
		System.out.print(bool.isBool());
	}

	public void prettyPrint(Comp comp) {
		prettyPrint(comp.getOprnd1());
		System.out.print(" " + comp.getOp() + " ");
		prettyPrint(comp.getOprnd2());
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
		System.out.print("(");
		prettyPrint(lam.getX());
		System.out.print(")");
		System.out.print(".\n");
		indent++;
		printIndent();
		prettyPrint(lam.getM());
		indent--;
		System.out.println();
	}

	public void prettyPrint(Let let) {
		System.out.print("let ");
		prettyPrint(let.getId());
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

	public void prettyPrint(Logical logical) {
		if (logical.getOprnd2() == null) {
			System.out.print(logical.getOp());
			prettyPrint(logical.getOprnd1());
		}
		else {
			prettyPrint(logical.getOprnd1());
			System.out.print(" " + logical.getOp() + " ");
			prettyPrint(logical.getOprnd2());
		}
	}

	public void prettyPrint(Num num) {
		System.out.print(num.getI());
	}

	public void prettyPrint(Params params) {
		if (params.getId() != null) {
			prettyPrint(params.getId());
		}
		if (params.getIds() != null) {
			System.out.print(" ");
			prettyPrint(params.getIds());
		}
	}

	public void prettyPrint(Str str) {
		System.out.print(str.getStr());
	}

	public void prettyPrint(TopLevel topLevel) {
		prettyPrint(topLevel.getId());
		System.out.print(" = ");
		prettyPrint(topLevel.getBody());

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
