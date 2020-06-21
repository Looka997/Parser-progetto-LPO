package lab09_04_20.parser.ast;

public class VarStmt extends AbstractAssignStmt {
    public VarStmt (Ident ident, Exp exp){
        super(ident, exp);
    }
}
