import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

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
        switch(typ.content)
        {
            case("int"):
                wert.type = Type.intType;
            case("double"):
                wert.type = Type.charType;
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
        else if (ausg.type == Type.charType) System.out.println(ausg.c);
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
                case "<":
                if (type == Type.intType)
                    erg.b = leftV.i < rightV.i;
                    break;
                case ">":
                if (type == Type.intType)
                    erg.b = leftV.i > rightV.i;
                    break;
            }
        }
        else if (op.kind == Token.Type.POP) {
            erg.type = type;
            switch (op.content) {
                case "+":
                if (type == Type.intType)
                    erg.i = leftV.i + rightV.i;
                    break;
                case "-":
                if (type == Type.intType)
                    erg.i = leftV.i - rightV.i;
                    break;
            }
        }
        else if (op.kind == Token.Type.LOP) {
            erg.type = type;
            switch (op.content) {
                case "*":
                if (type == Type.intType)
                    erg.i = leftV.i * rightV.i;
                    break;
                case "/": if (type == Type.intType)
                    erg.i = leftV.i / rightV.i;
                else if (type == Type.intType)
                    erg.i = leftV.i / rightV.i;
                    break;
                case "%": if (type == Type.intType)
                    erg.i = leftV.i % rightV.i;
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
                if (erg.type == Type.intType)
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
        //TODO
        return erg;
    }
}

class CharNode extends ExprNode {
    Token content;

    public CharNode(Token content) {
        super(content, content);
        this.content = content;
    }
    public String toString(String indent) {
        return indent+"Char: "+ content.content;
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return type = Type.charType;
    }
    public Value runExpr() {
        Value erg = new Value(); erg.type = Type.charType;
        erg.c = content.content.charAt(0);
        return erg;
    }
}

class StringNode extends ExprNode {
    Token content;

    public StringNode(Token content) {
        super(content, content);
        this.content = content;
    }
    public String toString(String indent) {
        return indent+"String: "+ content.content;
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return type = Type.stringType;
    }
    public Value runExpr() {
        Value erg = new Value(); erg.type = Type.stringType;
        erg.stg = content.content;
        return erg;
    }
}

class BoolNode extends ExprNode {
    Token content;

    public BoolNode(Token content) {
        super(content, content);
        this.content = content;
    }
    public String toString(String indent) {
        return indent+"Boolean: "+ content.content;
    }
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return type = Type.booleanType;
    }
    public Value runExpr() {
        Value erg = new Value(); erg.type = Type.booleanType;
        erg.b = Boolean.parseBoolean(content.content);
        return erg;
    }
}


//StateNode für den endlichen Automaten
//In der runExpr wird mit einem Boolean-Flag geprüft, 
//ob der State akzeptierend ist oder nicht
class StateNode extends ExprNode{
    boolean accept = false;
    Token content;

    public StateNode(Token content, boolean accept){
        super(content,content);
        this.content = content;
        this.accept = accept;
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


class RangeNode extends ExprNode{
    List<Pair<ExprNode,ExprNode>> entries;

    public RangeNode(List<Pair<ExprNode,ExprNode>> entries) {
        super(entries.get(0).getL().start,
            ((entries.get(entries.size()-1).getR())!=null) ? entries.get(entries.size()-1).getR().end : entries.get(0).getL().end);
        this.entries = entries;
    }

    @Override
    public String toString(String indent) {
        String ret = indent+"Range: ";
        for (Pair<ExprNode,ExprNode> pair : entries) {
            if(pair.getR() == null)
                ret += "'"+pair.getL()+"' ";
            else
                ret += "'"+pair.getL()+"'-'"+pair.getR()+"' ";
        }
        return  ret;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        //Alle Pairs müssen Char sein
        for (Pair<ExprNode,ExprNode> pair : entries) {
            if (pair.getL().semantischeAnalyseExpr(tabelle, errors) != Type.charType)
                errors.add(new SemanticError(start,end,"range-expressions must be chars"));
            if(pair.getR() != null)
                if(pair.getR().semantischeAnalyseExpr(tabelle, errors) != Type.charType)
                    errors.add(new SemanticError(start,end,"range-expressions must be chars"));
        }
        return type = Type.rangeType;
    }
    public Value runExpr() {
        Value erg = new Value(new Range());
        for (Pair<ExprNode,ExprNode> pair : entries) {
            if(pair.getR() == null) 
            {
                erg.r.add(pair.getL().runExpr().c);
            } else {
                erg.r.add(pair.getL().runExpr().c, 
                            pair.getR().runExpr().c);
            }
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
            return indent + "Transitionnode: $\""+ start.content +"\"--['" + r.toString() + "'-'"+ r.toString() + "'" + "]-->$\"" + end.content + "\"";
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
    List<ExprNode> list;
    
    
    public SetNode(Token type, List<ExprNode> list){
        super(type, type);
        this.type = type;
        this.list = list;
    }

    @Override
    public String toString(String indent) {
        return indent + "Set<" + type.content + "> = new HashSet<" + type.content + ">;" ;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        for (ExprNode i : list) {
            if(i.type.toString() != type.content.toString())
            {
                errors.add(new SemanticError(start, end, "Different Type in Set (Token: "+ i.type.toString() + " vs " + type.content.toString() + " Type of Set)"));
                return Type.errorType;
            }
        }
        return Type.setType;
    }

    @Override
    public Value runExpr() {
        Set<Value> verified = new HashSet<Value>();
        for(ExprNode i : list) {
                Value zw = i.runExpr();
                verified.add(zw);
        }
        Value erg = new Value(verified);

      return erg;
    }

}

class MapNode extends ExprNode {
    Token type;
    List<ExprNode> keyTypes;
    List<ExprNode> valueTypes;

    public MapNode(Token type, List<ExprNode> keyTypes, List<ExprNode> valueTypes){
        super(type, type);
        this.type = type;
        this.keyTypes = keyTypes;
        this.valueTypes = valueTypes;
    }

    @Override
    public String toString(String indent) {
        return indent + "Map<" + keyTypes.get(0).type.toString() + "," + valueTypes.get(0).type.toString() + ">;" ;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        if(keyTypes.size() != valueTypes.size())
        {
            errors.add(new SemanticError(start, end, "Different Size between Map-KeyTypes and Map-ValueTypes"));
        }

        for (ExprNode i : keyTypes) {
            if(i.type.toString() != type.content.toString())
            {
                errors.add(new SemanticError(start, end, "Different Type in Map-KeyTypes (Token: "+ i.type.toString() + " vs " + type.content.toString() + " Type of Map)"));
                return Type.errorType;
            }
        }

        for (ExprNode i : valueTypes) {
            if(i.type.toString() != type.content.toString())
            {
                errors.add(new SemanticError(start, end, "Different Type in Map-ValueTypes (Token: "+ i.type.toString() + " vs " + type.content.toString() + " Type of Map)"));
                return Type.errorType;
            }
        }
        return Type.mapType;
    }

    @Override
    public Value runExpr() {
        Map<Value, Value> verified = new HashMap<Value, Value>();
        for(int i = 0; i < keyTypes.size(); i++)
        {
            Value key = keyTypes.get(i).runExpr();
            Value valuetype = valueTypes.get(i).runExpr();
            verified.put(key, valuetype);
        }
        Value erg = new Value(verified);    
        return erg;
    }

}

class ArrayNode extends ExprNode {
    Token type;
    List<ExprNode> list;
    
    
    public ArrayNode(Token type, List<ExprNode> list){
        super(type, type);
        this.type = type;
        this.list = list;
    }

    @Override
    public String toString(String indent) {
        return indent + "Array";
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.arrayType;
    }

    @Override
    public Value runExpr() {
        List<Value> verified = new ArrayList<Value>();
        
        for(ExprNode i : list) {
                Value zw = i.runExpr();
               verified.add(zw);
                
        }
        Value erg = new Value(verified);
      return erg;
    }

}