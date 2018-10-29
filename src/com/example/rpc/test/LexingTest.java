package com.example.rpc.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.example.lib.LexerException;
import com.example.lib.Terminal;
import com.example.lib.TokenBuilder;
import com.example.rpc.Token;

public class LexingTest {
	private int lineno;
	private String endOfTok;
	private Object objEndOfTok;
	
	private BufferedReader br;
	private ArrayList<String> lineArr;
	private ArrayList<Terminal> lexer;
	
	private LinkedHashMap<String, TokenBuilder> tokenBuilders;
	
	private int testCaseLength = 7;
	
	@Test
	public void test() throws IOException, LexerException {
		tokenBuilders = new LinkedHashMap<>();
		
		init();
		
		for (int i = 1; i <= testCaseLength; i++) {
			String directory = System.getProperty("user.dir");
			FileReader fileReader = new FileReader(directory + "/testcase/simple_case/test" + String.format("%02d", i) + ".txt");
			lexer = new ArrayList<>();
			Lexing(fileReader);
			
			for (int j = 0; j < lexer.size(); j++) {
				FileReader resultReader = new FileReader(directory + "/testcase/simple_case/test_result/test" + String.format("%02d", i) + "_result.txt");
				BufferedReader resultBr = new BufferedReader(resultReader);
				String line;
				if ((line = br.readLine()) != null) {
					assertTrue(line == lexer.get(j).getSyntax().toString());
				}
			}
		}
	}
	
	public void init() {
		lex("[ \t\n]", text -> { return null; });
		lex("[0-9]+", text -> { return Token.NUM; });
		lex("[a-zA-Z]+[0-9]*", text -> {
			if (text.equalsIgnoreCase("lam"))
				return Token.LAM;
			else if (text.equalsIgnoreCase("let"))
				return Token.LET;
			else if (text.equalsIgnoreCase("in"))
				return Token.IN;
			else if (text.equalsIgnoreCase("end"))
				return Token.END;
			else if (text.equalsIgnoreCase("if"))
				return Token.IF;
			else if (text.equalsIgnoreCase("then"))
				return Token.THEN;
			else if (text.equalsIgnoreCase("else"))
				return Token.ELSE;
			else if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false"))
				return Token.BOOL;
			else
				return Token.ID; });
		
		lex("\\+", text -> { return Token.ADD; });
		lex("-", text -> { return Token.SUB; });
		lex("\\*", text -> { return Token.MUL; });
		lex("/", text -> { return Token.DIV; });
		
		lex("==", text -> { return Token.EQUAL; });
		lex("!=", text -> { return Token.NOTEQ; });
		
		lex("=", text -> { return Token.ASSIGN; });
		
		lex("\".*\"", text -> { return Token.STR; });
		lex("\\^[cs]", text -> { return Token.LOC; });
		lex("\\(", text -> { return Token.OPENPAREN; });
		lex("\\)", text -> { return Token.CLOSEPAREN; });
		lex("\\.", text -> { return Token.DOT; });
		lexEndToken("$", Token.END_OF_TOKEN);
	}

	public void lex(String regExp, TokenBuilder tb) {
		tokenBuilders.put(regExp, tb);
	}

	public void lexEndToken(String regExp, Object objEndOfTok) {
		endOfTok = regExp;
		this.objEndOfTok = objEndOfTok;
	}

	public void Lexing(Reader r) throws IOException, LexerException {
		br = new BufferedReader(r);
		lineArr = new ArrayList<>();

		String read_string = br.readLine();

		while (true) {
			String next_string = br.readLine();

			if (next_string != null) {
				lineArr.add(read_string + "\n");
				read_string = next_string;
			} else {
				lineArr.add(read_string);
				break;
			}
		}

		lineno = 1;
		TokenBuilder tb;

		Object[] keys = tokenBuilders.keySet().toArray();

		for (int idx = 0; idx < lineArr.size(); idx++) {
			String line = lineArr.get(idx);
			String str = "";

			// pattern matching
			int front_idx = 0;

			while (front_idx < line.length()) {
				int i;
				for (i = 0; i < keys.length; i++) {
					String regExp = (String) keys[i];
					Pattern p = Pattern.compile(regExp);
					Matcher matcher = p.matcher(line).region(front_idx, line.length());

					if (matcher.lookingAt()) {
						int startIdx = matcher.start();
						int endIdx = matcher.end();

//						System.out.println(startIdx +", " + endIdx);

						str = line.substring(startIdx, endIdx);
						matcher.region(endIdx, line.length());

						tb = tokenBuilders.get(regExp);
						if (tb.tokenBuilder(str) != null) {
							lexer.add(new Terminal(str, tb.tokenBuilder(str), startIdx, lineno));
						}

						str = "";

						front_idx = endIdx;
						break;
					}
				}
				if (i >= keys.length)
					throw new LexerException("No Pattern matching " + front_idx + ", " + line.substring(front_idx));
			}

			lineno++;
		}

		Terminal epsilon = new Terminal(endOfTok, objEndOfTok, -1, -1);
		lexer.add(epsilon);
	}

}
