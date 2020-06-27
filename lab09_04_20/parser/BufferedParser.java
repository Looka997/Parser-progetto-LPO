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
Stmt ::= 'var'? IDENT '=' Exp | 'print' Exp |  'if' '(' Exp ')' '{' StmtSeq '}' ('else' '{' StmtSeq '}')? | 'for' IDENT 'to' Exp '{' StmtSeq '}'
Exp ::= Eq ('&&' Eq)* | Exp '<' Exp | '#' Exp | 'seasonof' Exp
Eq ::= Add ('==' Add)*
Add ::= Mul ('+' Mul)*
Mul::= Atom ('*' Atom)*
Atom ::= '<<' Exp ',' Exp '>>' | 'fst' Atom | 'snd' Atom | '-' Atom | '!' Atom | BOOL | NUM | IDENT | SEASON | '(' Exp ')'
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
		var stmt = parseStmt();
		if (buf_tokenizer.tokenType() == STMT_SEP){
			nextToken();
			return new MoreStmt(stmt, parseStmtSeq());
		}
		return new SingleStmt(stmt);

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
		nextToken();
		return new PrintStmt(parseExp());
	}

	// parses the 'var' statement
	private VarStmt parseVarStmt() throws ParserException {
	    nextToken();
	    VarIdent ident = parseVarIdent();
	    consume(ASSIGN);
	    return new VarStmt(ident,parseExp());
	}

	// parses the assignment statement
	private AssignStmt parseAssignStmt() throws ParserException {
	    Ident ident = parseVarIdent();
	    consume(ASSIGN);
	    return new AssignStmt(ident, parseExp());
	}

	// parses the if_else statement
	private IfStmt parseIfStmt() throws ParserException {
	    nextToken();
	    match(OPEN_PAR);
	    Exp exp = parseRoundPar();
	    match(OPEN_BLOCK);
	    Block thenBlock = parseBlock();
	    if (buf_tokenizer.tokenType() == ELSE){
	    	nextToken();
	    	match(OPEN_BLOCK);
	    	return new IfStmt(exp, thenBlock, parseBlock());
		}
	    return new IfStmt(exp, thenBlock);

	}

	// parses a block statement
	private Block parseBlock() throws ParserException {
	    nextToken();
	    Block block = new Block(parseStmtSeq());
	    consume(CLOSE_BLOCK);
	    return block;
	}

	/*
	 * parses expressions, starting from the lowest precedence operator AND which is
	 * left-associative
	 */
	private Exp parseExp() throws ParserException {
	    Exp exp = parseEq();
	    while (buf_tokenizer.tokenType() == AND){
	    	nextToken();
	    	exp = new And(exp, parseEq() );
		}
	    return exp;
	}

	/*
	 * parses expressions, starting from the lowest precedence operator EQ which is
	 * left-associative
	 */
	private Exp parseEq() throws ParserException {
	    Exp exp = parseAdd();
	    while (buf_tokenizer.tokenType() == EQ){
	    	nextToken();
	    	exp = new Eq (exp, parseAdd());
		}
	    return exp;
	}

	/*
	 * parses expressions, starting from the lowest precedence operator PLUS which
	 * is left-associative
	 */
	private Exp parseAdd() throws ParserException {
	    Exp exp = parseMul();
	    while (buf_tokenizer.tokenType() == PLUS){
	    	nextToken();
	    	exp = new Add (exp, parseMul());
		}
	    return exp;
	}

	/*
	 * parses expressions, starting from the lowest precedence operator TIMES which
	 * is left-associative
	 */
	private Exp parseMul() throws ParserException {
		var exp = parseAtom();
		while (buf_tokenizer.tokenType() == TIMES){
			nextToken();
			exp = new Mul (exp, parseAtom());
		}
		return exp;
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
		match(NUM);
	    var intLiteral = new IntLiteral(buf_tokenizer.intValue());
	    nextToken();
	    return intLiteral;
	}

	// parses boolean literals
	private BoolLiteral parseBoolean() throws ParserException {
		match(BOOL);
		var boolLiteral = new BoolLiteral(buf_tokenizer.boolValue());
		nextToken();
		return boolLiteral;
	}

	// parses variable identifiers
	private VarIdent parseVarIdent() throws ParserException {
		match(IDENT);
		var varIdent = new VarIdent(buf_tokenizer.tokenString());
		nextToken();
		return varIdent;
	}

	// parses MINUS Atom
	private Sign parseMinus() throws ParserException {
		nextToken();
	    return new Sign(parseAtom());
	}

	// parses FST Atom
	private Fst parseFst() throws ParserException {
		nextToken();
		return new Fst(parseAtom());
	}

	// parses SND Atom
	private Snd parseSnd() throws ParserException {
		nextToken();
		return new Snd(parseAtom());
	}

	// parses NOT Atom
	private Not parseNot() throws ParserException {
		nextToken();
		return new Not(parseAtom());
	}

	// parses pairs
	private PairLit parsePairLit() throws ParserException {
	    nextToken();
	    Exp left = parseExp();
	    consume(EXP_SEP);
		Exp right = parseExp();
	    consume(END_PAIR);
	    return new PairLit(left, right);
	}

	// parses OPEN_PAR Exp CLOSE_PAR
	private Exp parseRoundPar() throws ParserException {
	    nextToken();
	    var exp = parseExp();
	    consume(CLOSE_PAR);
	    return exp;
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
