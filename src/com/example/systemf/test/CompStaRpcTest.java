package com.example.systemf.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.systemf.CompException;
import com.example.systemf.CompStaRpc;
import com.example.systemf.TyEnv;
import com.example.systemf.TypeCheckException;
import com.example.systemf.TypeChecker;
import com.example.systemf.ast.TopLevel;
import com.example.systemf.ast.Type;
import com.example.systemf.parser.Parser;

public class CompStaRpcTest {
	Parser parser;
	
	@Test
	public void test() throws IOException, ParserException, LexerException, TypeCheckException, CompException {
		parser = new Parser();
		String directory = System.getProperty("user.dir");
		recursiveRead(new File(directory + "/testcase/examples/systemF/"));
	}
	
	public void recursiveRead(File file) throws ParserException, IOException, LexerException, TypeCheckException, CompException {
		File[] listOfFiles = file.listFiles();
		
		for(File f: listOfFiles) {
			if (f.isFile()) {
				System.out.println("\n" + f);
				FileReader fileReader = new FileReader(f);
				TopLevel ex1 = (TopLevel) parser.Parsing(fileReader);
				Type ty1 = TypeChecker.checkTopLevel(ex1, new TyEnv());
				com.example.systemf.starpc.TopLevel compEx1 = CompStaRpc.compStaRpc(ex1);
				
				System.out.println(ex1);
				System.out.println("after compile:" + compEx1);
			}
		}
	}

}
