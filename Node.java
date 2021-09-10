import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import Bibliothek.*;


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








//Klassen aus Abstract-Klassen erstellt
class CUNode extends Node {
    List<Node> declAndStmnts =  new LinkedList<>();
    public CUNode() {
        super(null, null);
    }
    public void add(Node node) {
        declAndStmnts.add(node);
        if (start==null) start = node.start;
        end = node.end;
    }
     public String toString(String indent) {
        String res = indent+"CompilationUnit";
        for (Node node:declAndStmnts) res += "\n"+node.toString(indent+"\t");
        return res;
     }
     public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        SymbolTabelle global = new SymbolTabelle(null);
        for (Node node: declAndStmnts) node.semantischeAnalyse(global, errors);
     }
     public void run() {
         for (Node node: declAndStmnts) node.run();
     }
 }

 class DeclNode extends Node {
    Token typ;
    Token name;
    Value wert;

    public DeclNode(Token typ, Token name, Token end) {
        super(typ, end);
        this.typ = typ;
        this.name = name;
        wert = new Value();
        if (typ.content.equals("int")) wert.type = Type.intType;
        else wert.type = Type.doubleType;
        switch(typ.content)
        {
            case("int"):
                wert.type = Type.intType;
            case("double"):
                wert.type = Type.doubleType;
            case("boolean"):
                wert.type = Type.booleanType;
            case("state"):
                wert.type = Type.stateType;
            case("range"):
                wert.type = Type.rangeType;
            case("transition"):
                wert.type = Type.transitionType;
            case("epsilonTransition"):
                wert.type = Type.epsilonTransitionType;
            case("finiteAutomata"):
                wert.type = Type.finiteAutomataType;
            case("regularExpression"):
                wert.type = Type.regularExpressionType;
        }

    }
    public String toString(String indent) {
        return indent+"Declaration: "+typ.content+" "+name.content;
    }
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        tabelle.add(name.content, Type.getType(typ.content), this);
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
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        expr.semantischeAnalyseExpr(tabelle,errors);
    }
    public void run() {
        Value ausg = expr.runExpr();
        if (ausg.type == Type.booleanType) System.out.println(ausg.b);
        else if (ausg.type == Type.doubleType) System.out.println(ausg.d);
        else if (ausg.type == Type.intType) System.out.println(ausg.i);
        else if (ausg.type == Type.stateType) System.out.println(ausg.s);
        else if (ausg.type == Type.rangeType) System.out.println(ausg.r);
        else if (ausg.type == Type.transitionType) System.out.println(ausg.t);
        else if (ausg.type == Type.epsilonTransitionType) System.out.println(ausg.et);
        else if (ausg.type == Type.finiteAutomataType) System.out.println(ausg.fa);
        else if (ausg.type == Type.regularExpressionType) System.out.println(ausg.re);
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

    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) { }
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
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
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
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
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

class BlockNode extends StmntNode {
    List<Node> declAndStmnts =  new LinkedList<>();
    public BlockNode(Token start, Token end) {
        super(start, end);
    }
    public void add(Node node) {
        declAndStmnts.add(node);
    }
    public String toString(String indent) {
        String res = indent+"BlockStatement";
        for (Node node:declAndStmnts) res += "\n"+node.toString(indent+"\t");
        return res;
    }
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        SymbolTabelle lokal = new SymbolTabelle(tabelle);
        for (Node node: declAndStmnts) node.semantischeAnalyse(lokal, errors);
    }
    public void run() {
        for (Node node: declAndStmnts) node.run();
    }
}

class ExprStmntNode extends StmntNode {
    ExprNode expr;

    public ExprStmntNode(ExprNode expr, Token end) {
        super(expr.start, end);
        this.expr = expr;
    }
    public String toString(String indent) {
        return indent+"ExpressionStatement\n"+expr.toString(indent+"\t");
    }
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
       expr.semantischeAnalyseExpr(tabelle, errors);
    }
    public void run() {
        expr.run();
    }
}





















class NumberINode extends NumberNode {
    public NumberINode(Token content) {
        super(content);
    }
    public String toString(String indent) {
        return indent+"Integer: "+content.content;
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return type = Type.intType;
    }
    public Value runExpr() {
        Value erg = new Value(); erg.type = Type.intType;
        erg.i = Integer.parseInt(content.content);
        return erg;
    }
}

class NumberDNode extends NumberNode {
    public NumberDNode(Token content) {
        super(content);
    }
    public String toString(String indent) {
        return indent+"Double: "+content.content;
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return type = Type.doubleType;
    }
    public Value runExpr() {
        Value erg = new Value(); erg.type = Type.doubleType;
        erg.d = Double.parseDouble(content.content);
        return erg;
    }
}















class IdentifierNode extends ExprNode {
    Token content;
    DeclNode dn;
    // int i;

    public IdentifierNode(Token content) {
        super(content,content);
        this.content = content;
    }
    public String toString(String indent) {
        return indent+"Identifier: "+content.content;
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        TypeNodePair ergP = tabelle.find(content.content);
        Type erg;
        if (ergP==null) {
            errors.add(new SemanticError(start, end, "Unknown identifier "+content.content));
            erg = Type.errorType;
        }
        else {
            erg = ergP.type;
            dn = ergP.node;
        }
        return type = erg;
    }
    public Value runSetExpr(Value v) {
        dn.wert = v.copy();
        return v;
    }

    public Value runExpr() {
        Value erg = dn.wert.copy();
        return erg;
    }
}

class BinOpNode extends ExprNode {
    Token op;
    ExprNode left, right;

    public BinOpNode(Token op, ExprNode left, ExprNode right) {
        super(left.start, right.end);
        this.left = left;
        this.right = right;
        this.op = op;
    }
    public String toString(String indent) {
        return indent+"Binary Operator: "+op.content + " type "+type
                + "\n"+left.toString(indent+"\t")
                + "\n"+right.toString(indent+"\t");
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        Type leftT = left.semantischeAnalyseExpr(tabelle, errors);
        Type rightT = right.semantischeAnalyseExpr(tabelle, errors);
        if (leftT!=rightT) {
            if (leftT == Type.kgT(leftT, rightT))
                right = new CastNode(right, Type.kgT(leftT, rightT));
            else left = new CastNode(left, Type.kgT(leftT, rightT));
        }
        if (op.kind == Token.Type.COMP) return type = Type.booleanType;
        else return type = Type.kgT(leftT, rightT);
    }
    public Value runExpr() {
        Value leftV = left.runExpr();
        Value rightV = right.runExpr();
        Value erg  = new Value();

        if (op.kind == Token.Type.COMP) {
            erg.type = Type.booleanType;
            switch (op.content) {
                case "<": if (type == Type.doubleType)
                    erg.b = leftV.d < rightV.d;
                else if (type == Type.intType)
                    erg.b = leftV.i < rightV.i;
                    break;
                case ">": if (type == Type.doubleType)
                    erg.b = leftV.d > rightV.d;
                else if (type == Type.intType)
                    erg.b = leftV.i > rightV.i;
                    break;
            }
        }
        else if (op.kind == Token.Type.POP) {
            erg.type = type;
            switch (op.content) {
                case "+": if (type == Type.doubleType)
                    erg.d = leftV.d + rightV.d;
                else if (type == Type.intType)
                    erg.i = leftV.i + rightV.i;
                    break;
                case "-": if (type == Type.doubleType)
                    erg.d = leftV.d - rightV.d;
                else if (type == Type.intType)
                    erg.i = leftV.i - rightV.i;
                    break;
            }
        }
        else if (op.kind == Token.Type.LOP) {
            erg.type = type;
            switch (op.content) {
                case "*": if (type == Type.doubleType)
                    erg.d = leftV.d * rightV.d;
                else if (type == Type.intType)
                    erg.i = leftV.i * rightV.i;
                    break;
                case "/": if (type == Type.doubleType)
                    erg.d = leftV.d / rightV.d;
                else if (type == Type.intType)
                    erg.i = leftV.i / rightV.i;
                    break;
                case "%": if (type == Type.doubleType)
                    erg.d = leftV.d % rightV.d;
                else if (type == Type.intType)
                    erg.i = leftV.i % rightV.i;
                    break;
            }
        }
        else if (op.kind == Token.Type.SETTO) {
            erg = ((IdentifierNode)left).runSetExpr(rightV).copy();
            erg.type = type;
        }
        return erg;
    }
}

class UnOpNode extends ExprNode {
    Token op;
    ExprNode kid;

    public UnOpNode(Token op, ExprNode kid) {
        super(op, kid.end); // pre-ops only
        this.kid = kid;
        this.op = op;
    }
    public String toString(String indent) {
        return indent+"Unary Operator: "+op.content
                + "\n"+kid.toString(indent+"\t");
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        Type kidT = kid.semantischeAnalyseExpr(tabelle, errors);
        return type = kidT; // todo: testen ob ! -> boolean
    }
    public Value runExpr() {
        Value leftV = kid.runExpr();
        Value erg = leftV.copy();
        switch (op.content) {
            case "-":
                if (erg.type == Type.doubleType)
                    erg.d = - leftV.d ;
                else if (erg.type == Type.intType)
                    erg.i = -leftV.i;
                break;
            case "!": if (erg.type == Type.booleanType)
                erg.b = !leftV.b;
                break;
        }
        return erg;
    }
}

class CastNode extends ExprNode {
    Type castTo;
    ExprNode castNode;
    public CastNode(ExprNode node, Type targetType) {
        super(node.start, node.end);
        castNode = node;
        type = castTo = targetType;
    }

    @Override
    public String toString(String indent) {
        return "CastNode";
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return castTo;
    }
    public Value runExpr() {
        Value erg = castNode.runExpr().copy();
        erg.type = castTo;
        erg.d = erg.i;
        return erg;
    }
}


//StateNode für den endlichen Automaten
//In der runExpr wird mit einem Boolean-Flag geprüft, 
//ob der State akzeptierend ist oder nicht
class StateNode extends ExprNode{
    boolean accept = false;
    Token content;

    public StateNode(Token content){
        super(content,content);
        this.content = content;
    }
    public String toString(String indent){
        return indent+"State "+content.content;
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors){
        return type = Type.stateType;
    }
    public Value runExpr(){
        Value erg = new Value();
        erg.type = Type.stateType;
        if(accept)
            erg.s = new State(content.content,1);
        else
            erg.s = new State(content.content);
        return erg;
    }
}

//RangeNode mit zwei Charakter-Token
//Unterscheidung in der runExpr, ob die beiden Charakter-Token gleich sind oder nicht
//Somit wird die Range unterschieden
class RangeNode extends ExprNode {
    Token charA = null;
    Token charB = null;

    public RangeNode(Token contentA, Token contentB) {
        super(contentA, contentB);
        this.charA = contentA;
        this.charB = contentB;
    }

    @Override
    public String toString(String indent) {
        return indent+"Range from"+ charA.content + "-" + charB.content;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return type = Type.rangeType;
    }
    public Value runExpr() {
        Value erg;
        if(charA == charB) 
        {
            erg = new Value(new Range(charA.content.charAt(0)));
        } else {
            erg = new Value(new Range(charA.content.charAt(0), charB.content.charAt(0)));
        }
        return erg;
    }
}



//Transitionnode beinhaltet zwei Konstruktoren:
//Normaler Übergang: StartState -- [Range] --> EndState
//Epsilon-Übergang : StartState ---> EndState
//Bei der runExpr() werden die runExpr des State-& RangeNodes aufgerufen 
//und zwischen EpsilonTransition und normaler Transition unterschieden
class TransitionNode extends ExprNode {
    StateNode start = null;
    StateNode end = null;
    RangeNode r = null;

    public TransitionNode(StateNode start, StateNode end, RangeNode r) {
        super(start.content, end.content);
        this.r = r;
    }

    public TransitionNode(StateNode start, StateNode end) {
        super(start.content, end.content);
    }

    @Override
    public String toString(String indent) {
        if(r == null)
        {
        return indent + "Transitionnode: $\""+ start.content +"\"--->$\"" + end.content + "\"";
        } 
        else
        {
            return indent + "Transitionnode: $\""+ start.content +"\"--['" + r.charA.content + "'-'"+ r.charB.content + "'" + "]-->$\"" + end.content + "\"";
        }
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.transitionType;
    }
    public Value runExpr() {
        Value erg;

        if(r == null)
        {
            erg = new Value(new EpsilonTransition(start.runExpr().s, end.runExpr().s));
        } 
        else 
        {
            erg = new Value(new Transition(start.runExpr().s, end.runExpr().s, r.runExpr().r));
        }
        return erg;
    }
}


//FiniteAutomataNode gibt einen FiniteAutomata mit einem StartState wieder.
//Transitionen werden über den Parser an den FiniteAutomata gebunden
class FiniteAutomataNode extends ExprNode{
    StateNode start = null;

    public FiniteAutomataNode(StateNode start) {
        super(start.content, start.content);
    }

    @Override
    public String toString(String indent) {
        
        return indent + "FiniteAutomataNode: <$\"" + start.content + ", {}>";
    }
    
    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.finiteAutomataType;
    }

    @Override
    public Value runExpr() {
        Value erg = new Value(new FiniteAutomata(start.runExpr().s));
        return erg;
    }

}




class RegularExpressionNode extends ExprNode {
    String operation = null;
    RegularExpressionNode left;
    RegularExpressionNode right;

    public RegularExpressionNode(RegularExpressionNode left, RegularExpressionNode right) {
        super(left.start, right.end);
    }

    @Override
    public String toString(String indent) {
        
        return null;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.regularExpressionType;
    }

    @Override
    public Value runExpr() {
        //Value erg;
        if(operation == "or")
        {
           // erg = new Value(new )
        }
        return null;
    }
    
}

class SetNode extends ExprNode {
    Token type;
    List<Token> list;

    public SetNode(Token type, List<Token> list){
        super(type, type);
        this.type = type;
        this.list = list;
    }

    @Override
    public String toString(String indent) {
        return indent + "Set<" + type.kind.toString() + "> = new HashSet<" + type.kind.toString() + ">;" ;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        for (Token i : list) {
            if(i.kind.toString() != type.kind.toString())
            {
                errors.add(new SemanticError(start, end, "Different Type in Set (Token: "+ i.kind.toString() + " vs " + type.kind.toString() + " Type of Set)"));
                return Type.errorType;
            }
        }
        return Type.setType;
    }

    @Override
    public Value runExpr() {
        //Type t = Type.getType(type.content);
        //Value typeValue = new Value(t, (Object) type.content);
        Set<Value> verified = new HashSet<Value>();
        Value erg = new Value(verified);

      return erg;
    }

}

class MapNode extends ExprNode {
    Token type;
    List<Token> keyTypes;
    List<Token> valueTypes;

    public MapNode(Token type, List<Token> keyTypes, List<Token> valueTypes){
        super(type, type);
        this.type = type;
        this.keyTypes = keyTypes;
        this.valueTypes = valueTypes;
    }

    @Override
    public String toString(String indent) {
        return indent + "Map<" + keyTypes.get(0).kind.toString() + "," + valueTypes.get(0).kind.toString() + ">;" ;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        for (Token i : keyTypes) {
            if(i.kind.toString() != type.kind.toString())
            {
                errors.add(new SemanticError(start, end, "Different Type in Map-KeyTypes (Token: "+ i.kind.toString() + " vs " + type.kind.toString() + " Type of Map)"));
                return Type.errorType;
            }
        }

        for (Token i : valueTypes) {
            if(i.kind.toString() != type.kind.toString())
            {
                errors.add(new SemanticError(start, end, "Different Type in Map-ValueTypes (Token: "+ i.kind.toString() + " vs " + type.kind.toString() + " Type of Map)"));
                return Type.errorType;
            }
        }
        return Type.mapType;
    }

    @Override
    public Value runExpr() {
        Map<Value, Value> verified = new HashMap<Value, Value>();
        Value erg = new Value(verified);    
        return erg;
    }

}