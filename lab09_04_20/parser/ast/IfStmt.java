package lab09_04_20.parser.ast;

import static java.util.Objects.requireNonNull;

public class IfStmt implements Stmt {
    private final Exp exp; // non-optional
    private final Block thenBlock; // non-optional
    private final Block elseBlock; // optional

	public IfStmt(Exp exp, Block thenBlock, Block elseBlock) {
	    this.exp = requireNonNull(exp);
	    this.thenBlock = requireNonNull(thenBlock);
	    this.elseBlock = elseBlock;
	}

	public IfStmt(Exp exp, Block thenBlock) {
		this.exp = requireNonNull(exp);
		this.thenBlock = requireNonNull(thenBlock);
		this.elseBlock = null;
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + exp + "," + thenBlock + "," + elseBlock + ")";
	}

}
