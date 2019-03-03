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
import com.example.systemf.ast.Term;
import com.example.systemf.ast.Type;
import com.example.systemf.parser.Parser;

public class TypeCheckTest {
	Parser parser;
	
	@Test
	public void test() throws IOException, ParserException, LexerException, TypeCheckException {
		parser = new Parser();
		String directory = System.getProperty("user.dir");
		recursiveRead(new File(directory + "/testcase/examples/systemF/"));
		duplicateTypeVarFail();
	}
	
	public void recursiveRead(File file) throws ParserException, IOException, LexerException, TypeCheckException {
		File[] listOfFiles = file.listFiles();
		
		for(File f: listOfFiles) {
			if (f.isFile()) {
				System.out.println("\n" + f);
				FileReader fileReader = new FileReader(f);
				Term ex1 = (Term) parser.Parsing(fileReader);
				Type ty1 = TypeChecker.check(ex1, new TyEnv());
				System.out.println(ex1);
				System.out.println("Type: " + ty1.toString());
			}
		}
	}
	
	public void duplicateTypeVarFail() throws ParserException, IOException, LexerException {
		boolean flag = false;
		try {
			String directory = System.getProperty("user.dir");
			File f = new File(directory + "/testcase/examples/systemF/failure/example_duplicate_typevar_fail.txt");
			
			System.out.println("\n" + f);
			FileReader fileReader = new FileReader(f);
			Term ex1 = (Term) parser.Parsing(fileReader);
			Type ty1 = TypeChecker.check(ex1, new TyEnv());
			System.out.println(ex1);
			System.out.println("Type: " + ty1.toString());
		} catch(TypeCheckException e) {
			flag = true;
		}
		
		assert(flag);
	}

}
