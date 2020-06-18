package lab09_04_20.parser;

import lab09_04_20.parser.ast.Prog;

public interface Parser extends AutoCloseable {

	Prog parseProg() throws ParserException;

}