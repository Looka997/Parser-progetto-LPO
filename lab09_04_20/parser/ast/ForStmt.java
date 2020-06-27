package lab09_04_20.parser.ast;

import static java.util.Objects.requireNonNull;

public class ForStmt implements Stmt{
    private final Ident ident; // non-optional
    private final Exp exp; // non-optional
    private final Block body; // non-optional

    public ForStmt(Ident ident, Exp exp, Block body) {
        this.ident = requireNonNull(ident);
        this.exp = requireNonNull(exp);
        this.body = requireNonNull(body);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + ident + "," + exp  + "," + body + ")";
    }

}
