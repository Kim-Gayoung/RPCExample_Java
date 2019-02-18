package com.example.systemf;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.TopLevel;
import com.example.systemf.ast.Type;
import com.example.systemf.parser.Parser;

public class Main {
	public static void main(String[] args) throws ParserException, IOException, LexerException, TypeCheckException {
		Parser parser = new Parser();

		System.out.println("1: File, the other: Console");
		System.out.print("Enter the number: ");
		String select = new Scanner(System.in).next();
		
		Term ex1;
		
		if (select.equals("1")) {
			System.out.print("Enter a file name: ");
			String fileName = new Scanner(System.in).next();

			FileReader fileReader = new FileReader("./testcase/" + fileName);
			Scanner scan = new Scanner(fileReader);

			while (scan.hasNext()) {
				System.out.println(scan.nextLine());
			}
			System.out.println();

			fileReader = new FileReader("./testcase/" + fileName);
			ex1 = parser.Parsing(fileReader);
		}
		else {
			ex1 = parser.Parsing(new InputStreamReader(System.in));
		}
		System.out.println(ex1.toString());
		
		Type checkTy = TypeChecker.checkTopLevel((TopLevel) ex1, new TyEnv());
		System.out.println("Type: " + checkTy);
	}
}
