package com.example.systemf;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.systemf.ast.Term;
import com.example.systemf.ast.Type;
import com.example.systemf.parser.Parser;
import com.example.systemf.stacs.CompStaCs;
import com.example.systemf.stacs.FunStore;
import com.example.systemf.starpc.CompStaRpc;
import com.example.utils.TripleTup;

public class Main {
	public static void main(String[] args) throws ParserException, IOException, LexerException, TypeCheckException, CompException {
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
		
		Type checkTy = TypeChecker.check(ex1, new TyEnv());
		System.out.println("Type: " + checkTy);
		
		com.example.systemf.sta.ast.Term compStaRpcTerm = CompStaRpc.compStaRpc((Term) ex1);
		System.out.println(compStaRpcTerm);
		
		TripleTup<com.example.systemf.sta.ast.Term, FunStore, FunStore> compStaCsTerm = CompStaCs.cloConvTerm((com.example.systemf.sta.ast.Term) compStaRpcTerm);
		
		System.out.println(compStaCsTerm.getFirst());
		System.out.println("client funstore: ");
		System.out.println(compStaCsTerm.getSecond());
		System.out.println("server funstore: ");
		System.out.println(compStaCsTerm.getThird());
	}
}
