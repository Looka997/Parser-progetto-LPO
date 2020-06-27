package lab09_04_20.parser;

import lab09_04_20.parser.ast.Season;

import java.io.IOException;

public interface Tokenizer extends AutoCloseable {

	TokenType next() throws TokenizerException;

	TokenType tokenType();

	String tokenString();

	int intValue();

	boolean boolValue();

	Season seasonValue();

	public void close() throws IOException;

	int getLineNumber();

}