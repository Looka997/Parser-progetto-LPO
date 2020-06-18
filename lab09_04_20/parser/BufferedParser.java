package lab09_04_20.parser;

import static java.util.Objects.requireNonNull;
import static lab09_04_20.parser.TokenType.*;
import static java.lang.System.err;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lab09_04_20.parser.ast.*;

/*
Prog ::= StmtSeq EOF
StmtSeq ::= Stmt (';' StmtSeq)?
Stmt ::= 'var'? IDENT '=' Exp | 'print' Exp |  'if' '(' Exp ')' '{' StmtSeq '}' ('else' '{' StmtSeq '}')? 
Exp ::= Eq ('&&' Eq)* 
Eq ::= Add ('==' Add)*
Add ::= Mul ('+' Mul)*
Mul::= Atom ('*' Atom)*
Atom ::= '<<' Exp ',' Exp '>>' | 'fst' Atom | 'snd' Atom | '-' Atom | '!' Atom | BOOL | NUM | IDENT | '(' Exp ')'
*/

public class BufferedParser implements Parser {

	private final BufferedTokenizer buf_tokenizer; // the buffered tokenizer used by the parser

	/*
	 * reads the next token through the buffered tokenizer associated with the
	 * parser; TokenizerExceptions are chained into corresponding ParserExceptions
	 */
	private void nextToken() throws ParserException {
		try {
			buf_tokenizer.next();
		} catch (TokenizerException e) {
			throw new ParserException(e);
		}
	}

	// decorates error message with the corresponding line number
	private String line_err_msg(String msg) {
		return "on line " + buf_tokenizer.getLineNumber() + ": " + msg;
	}

	/*
	 * checks whether the token type of the currently recognized token matches
	 * 'expected'; if not, it throws a corresponding ParserException
	 */
	private void match(TokenType expected) throws ParserException {
		final var found = buf_tokenizer.tokenType();
		if (found != expected)
			throw new ParserException(line_err_msg(
					"Expecting " + expected + ", found " + found + "('" + buf_tokenizer.tokenString() + "')"));
	}

	/*
	 * checks whether the token type of the currently recognized token matches
	 * 'expected'; if so, it reads the next token, otherwise it throws a
	 * corresponding ParserException
	 */
	private void consume(TokenType expected) throws ParserException {
		match(expected);
		nextToken();
	}

	// throws a ParserException because the current token was not expected
	private void unexpectedTokenError() throws ParserException {
		throw new ParserException(line_err_msg(
				"Unexpected token " + buf_tokenizer.tokenType() + "('" + buf_tokenizer.tokenString() + "')"));
	}

	// associates the parser with a corresponding non-null buffered tokenizer
	public BufferedParser(BufferedTokenizer tokenizer) {
		this.buf_tokenizer = requireNonNull(tokenizer);
	}

	@Override
	public Prog parseProg() throws ParserException {
		nextToken(); // one look-ahead symbol
		var prog = new ProgClass(parseStmtSeq());
		match(EOF); // last token must have type EOF
		return prog;
	}

	@Override
	public void close() throws IOException {
		if (buf_tokenizer != null)
			buf_tokenizer.close();
	}

	// parses a non empty sequence of statements, MoreStmt binary operator is right
	// associative
	private StmtSeq parseStmtSeq() throws ParserException {
	    // completare
	}

	// parses statements
	private Stmt parseStmt() throws ParserException {
		switch (buf_tokenizer.tokenType()) {
		default:
			unexpectedTokenError();
		case PRINT:
			return parsePrintStmt();
		case VAR:
			return parseVarStmt();
		case IDENT:
			return parseAssignStmt();
		case IF:
			return parseIfStmt();
		}
	}

	// parses the 'print' statement
	private PrintStmt parsePrintStmt() throws ParserException {
	    // completare
	}

	// parses the 'var' statement
	private VarStmt parseVarStmt() throws ParserException {
	    // completare
	}

	// parses the assignment statement
	private AssignStmt parseAssignStmt() throws ParserException {
	    // completare
	}

	// parses the if_else statement
	private IfStmt parseIfStmt() throws ParserException {
	    // completare
	}

	// parses a block statement
	private Block parseBlock() throws ParserException {
	    // completare
	}

	/*
	 * parses expressions, starting from the lowest precedence operator AND which is
	 * left-associative
	 */
	private Exp parseExp() throws ParserException {
	    // completare
	}

	/*
	 * parses expressions, starting from the lowest precedence operator EQ which is
	 * left-associative
	 */
	private Exp parseEq() throws ParserException {
	    // completare
	}

	/*
	 * parses expressions, starting from the lowest precedence operator PLUS which
	 * is left-associative
	 */
	private Exp parseAdd() throws ParserException {
	    // completare
	}

	/*
	 * parses expressions, starting from the lowest precedence operator TIMES which
	 * is left-associative
	 */
	private Exp parseMul() throws ParserException {
	    // completare
	}

	// parses expressions of type Atom
	private Exp parseAtom() throws ParserException {
		switch (buf_tokenizer.tokenType()) {
		default:
			unexpectedTokenError();
		case NUM:
			return parseNum();
		case IDENT:
			return parseVarIdent();
		case MINUS:
			return parseMinus();
		case OPEN_PAR:
			return parseRoundPar();
		case BOOL:
			return parseBoolean();
		case NOT:
			return parseNot();
		case START_PAIR:
			return parsePairLit();
		case FST:
			return parseFst();
		case SND:
			return parseSnd();
		}
	}

	// parses natural literals
	private IntLiteral parseNum() throws ParserException {
	    // completare
	}

	// parses boolean literals
	private BoolLiteral parseBoolean() throws ParserException {
	    // completare
	}

	// parses variable identifiers
	private VarIdent parseVarIdent() throws ParserException {
	    // completare
	}

	// parses MINUS Atom
	private Sign parseMinus() throws ParserException {
	    // completare
	}

	// parses FST Atom
	private Fst parseFst() throws ParserException {
	    // completare
	}

	// parses SND Atom
	private Snd parseSnd() throws ParserException {
	    // completare
	}

	// parses NOT Atom
	private Not parseNot() throws ParserException {
	    // completare
	}

	// parses pairs
	private PairLit parsePairLit() throws ParserException {
	    // completare
	}

	// parses OPEN_PAR Exp CLOSE_PAR
	private Exp parseRoundPar() throws ParserException {
	    // completare
	}

	private static BufferedReader tryOpenInput(String inputPath) throws FileNotFoundException {
		return new BufferedReader(inputPath == null ? new InputStreamReader(System.in) : new FileReader(inputPath));
	}

	public static void main(String[] args) {
		try (var buf_reader = tryOpenInput(args.length > 0 ? args[0] : null);
				var buf_tokenizer = new BufferedTokenizer(buf_reader);
				var buf_parser = new BufferedParser(buf_tokenizer);) {
			var prog = buf_parser.parseProg();
			System.out.println(prog);
		} catch (IOException e) {
			err.println("I/O error: " + e.getMessage());
		} catch (ParserException e) {
			err.println("Syntax error " + e.getMessage());
		} catch (Throwable e) {
			err.println("Unexpected error.");
			e.printStackTrace();
		}

	}

}
