import java.util.LinkedList;
import java.util.List;


//Abstrakte Klassen für alle folgenden Node-Typen
public abstract class Node {
    Token start, end;

    public Node(Token start, Token end) {
        this.end = end;
        this.start = start;
    }
    public abstract String toString(String indent);

    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {}

    public void run() {}
}

abstract class ExprNode extends Node {
    Type type;

    public ExprNode(Token start, Token end) {
        super(start, end);
    }
    public abstract Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors);
    public abstract Value runExpr();
    public void run() { runExpr(); }
}

abstract class NumberNode extends ExprNode {
    Token content;
    public NumberNode(Token content) {
        super(content,content);
        this.content = content;
    }
}

abstract class StmntNode extends Node {
    public StmntNode(Token start, Token end) {
        super(start,end);
    }
}


class PrintNode extends StmntNode {
    ExprNode expr;

    public PrintNode(Token start, Token end, ExprNode expr) {
        super(start, end);
        this.expr = expr;
    }
    public String toString(String indent) {
        return indent+"PrintStatement\n"
                +expr.toString(indent+"\t");
    }
    public void semantischeAnalyse(SymbolTabelle tabelle, List<CompilerError> errors) {
        expr.semantischeAnalyseExpr(tabelle,errors);
    }
    public void run() {
        Value ausg = expr.runExpr();
        if (ausg.type == Type.booleanType) System.out.println(ausg.b);
        else if (ausg.type == Type.doubleType) System.out.println(ausg.d);
        else if (ausg.type == Type.intType) System.out.println(ausg.i);
        else System.out.println("unknown type");
    }
}



class EmptyStmntNode extends StmntNode {
    public EmptyStmntNode(Token start) {
        super(start,start);
    }
    public String toString(String indent) {
        return indent+"EmptyStmnt";
    }

    public void semantischeAnalyse(SymbolTabelle tabelle, List<CompilerError> errors) { }
}




class IfNode extends StmntNode {
    ExprNode expr;
    StmntNode stmnt;
    StmntNode elseStmnt;

    public IfNode(Token start, ExprNode expr, StmntNode stmnt, StmntNode elseStmnt) {
        super(start, elseStmnt!=null ? elseStmnt.end : stmnt.end);
        this.expr = expr;
        this.stmnt = stmnt;
        this.elseStmnt = elseStmnt;
    }
    public String toString(String indent) {
        return indent+"IfStatement\n"
                +expr.toString(indent+"\t")+"\n"
                +stmnt.toString(indent+"\t")
                + (elseStmnt!=null ? "\n"+elseStmnt.toString(indent+"\t") : "");
    }
    public void semantischeAnalyse(SymbolTabelle tabelle, List<CompilerError> errors) {
        if (expr.semantischeAnalyseExpr(tabelle,errors) != Type.booleanType)
        {
            errors.add(new SemanticError(start,end,"if-expression must be boolean"));
        }
        stmnt.semantischeAnalyse(tabelle, errors);
        if (elseStmnt!=null) elseStmnt.semantischeAnalyse(tabelle, errors);
    }
    public void run() {
        Value bed = expr.runExpr();
        if (bed.b) stmnt.run();
        else if (elseStmnt!=null) elseStmnt.run();
    }
}

class WhileNode extends StmntNode {
    ExprNode expr;
    StmntNode stmnt;

    public WhileNode(Token start, ExprNode expr, StmntNode stmnt) {
        super(start, stmnt.end);
        this.expr = expr;
        this.stmnt = stmnt;
    }
    public String toString(String indent) {
        return indent+"WhileStatement\n"
                +expr.toString(indent+"\t")+"\n"
                +stmnt.toString(indent+"\t");
    }
    public void semantischeAnalyse(SymbolTabelle tabelle, List<CompilerError> errors) {
        if (expr.semantischeAnalyseExpr(tabelle, errors) != Type.booleanType)
        {
            errors.add(new SemanticError(start,end,"while-condition must be boolean"));
        }
        stmnt.semantischeAnalyse(tabelle, errors);
    }
    public void run() {
        Value bed = expr.runExpr();
        while (bed.b) stmnt.run();
    }
}


