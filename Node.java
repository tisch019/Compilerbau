import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;


//Abstrakte Klassen für alle folgenden Node-Typen zur Erstellung des AST
//Die abstrakte Klasse Node ist die Oberklasse aller Knotenpunkte im AST
public abstract class Node {
    Token start, end;

    /**
     * Erzeugt einen Knotenpunkt im AST
     * @param start Beginn der Fehlermeldung, falls in einem Token-Abschnitt ein semantischer Fehler auftritt
     * @param end Ende der Fehlermeldung
     * @return Objekt der Klasse Node
    */
    public Node(Token start, Token end) {
        this.end = end;
        this.start = start;
    }

    /**
     * Ausgabe zum Debuggen / Fehlersuche
     * @param indent Einzug für die Ausgabe
     * @return Strukturierten String mit gewünschten Inhalt
     */
    public abstract String toString(String indent);

    /**
     * Dient zur semantischen Korrektheit (Typ-Überprüfung)
     * @param tabelle Symbole, die sich in einem Code-Abschnitt 
     * befinden und neue Symbolehinzugefügt werden können
     * @param errors Falls Fehler auftreten, werden die Fehler in einer Liste abgespeichert
     */
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {}

    /**
     * Methode zur Erzeugung eines Knotens
     */
    public void run() {}
}


/**
 * Abstrakte Klasse ExprNode entspricht der Klasse Node
 * mit einem Objekt von Typ Type als Erweiterung zur Speicherung 
 * des Typs
 */
abstract class ExprNode extends Node {
    Type type;

    public ExprNode(Token start, Token end) {
        super(start, end);
    }
    public abstract Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors);

    /**
     * Erzeugung eines Value-Objektes, der 
     * alle Objekt-Typen der Skriptsprache zusammenfasst
     * @return Neues Objekt der Skriptsprache verpackt im Value-Objekt
     */
    public abstract Value runExpr();

    public void run() { runExpr(); }
}

/**
 * NumberNode dient als Oberklasse für Integer-Deklarationen
 * Beinhaltet einen Token mit dem Inhalt des Integers
 * Kann auch auf Double, Float erweitert werden (nicht Teil der Skriptsprache)
 */
abstract class NumberNode extends ExprNode {
    Token content;
    public NumberNode(Token content) {
        super(content,content);
        this.content = content;
    }
}

/**
 * Abstrakte Klasse für Statements zur besseren Struktur der Node-Hierarchie
 * (If, While, Block, Print, Emptystatements, Expressions)
 */
abstract class StmntNode extends Node {
    public StmntNode(Token start, Token end) {
        super(start,end);
    }
}

//Folgend Node-Deklarationen, die von den abstrakten Nodes erben

/**
 * CUNode ist die Wurzel des Node-Hierarchie und beinhaltet eine Liste
 * der Nodes, die abgearbeitet werden müssen
 */
class CUNode extends Node {
    List<Node> declAndStmnts =  new LinkedList<>();
    public CUNode() {
        super(null, null);
    }

    /**
     * Fügt der Liste einen Knoten (Knoten) hinzu
     * @param node Knoten, der hinzugefügt werden soll
     */
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

    /**
     * Legt eine globale Symboltabelle an mit Aufruf der 
     * semantischen Analyse der abzuarbeitenden Knoten
     */
     public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        SymbolTabelle global = new SymbolTabelle(null);
        for (Node node: declAndStmnts) node.semantischeAnalyse(global, errors);
     }

     /**
      * Für jeden Knoten-Typ wird die jeweilige run-Methode aufgerufen
      */
     public void run() {
         for (Node node: declAndStmnts) node.run();
     }
 }

 /**
  * Deklarierungen werden im DeclNode festgehalten
  * (Beispiel: int a = 10; --> typ name = wert)
  * Zudem können auch generische Typen aufgelistet werden
  */
 class DeclNode extends Node {
    Token typ;
    List<Token> genericTypes;
    Token name;
    Value wert;

    /**
     * Konstruktor des DeclNoce
     * @param typ Typ des Knoten
     * @param genericTypes Mögliche Generics
     * @param name Name der Deklaration
     * @param end Ende der Deklaration
     * Dem Typ des Value wert wird über das Token typ ermittelt
     */
    public DeclNode(Token typ, List<Token> genericTypes, Token name, Token end) {
        super(typ, end);
        this.typ = typ;
        this.genericTypes = genericTypes;
        this.name = name;
        wert = new Value();
        switch(typ.content)
        {
            case("int"):
                wert.type = Type.intType;break;
            case("char"):
                wert.type = Type.charType;break;
            case("boolean"):
                wert.type = Type.booleanType;break;
            case("String"):
                wert.type = Type.stringType;break;
            case("State"):
                wert.type = Type.stateType;break;
            case("Range"):
                wert.type = Type.rangeType;break;
            case("Transition"):
                wert.type = Type.transitionType;break;
            case("epsilonTransition"):
                wert.type = Type.epsilonTransitionType;break;
            case("FA"):
                wert.type = Type.finiteAutomataType;break;
            case("RE"):
                wert.type = Type.regularExpressionType;break;
            case("Set"):
                wert.type = Type.setType;break;
            case("Map"):
                wert.type = Type.mapType;break;
            case("Or"):
                wert.type = Type.orType;break;
            case("Concat"):
                wert.type = Type.concatType;break;
            case("Star"):
                wert.type = Type.starType;break;
            case("RangeExpr"):
                wert.type = Type.rangeExprType;break;
            case("EmptyWord"):
                wert.type = Type.emptyWordType;break;
        }

    }

    /**
     * Ausgabe der Declaration mit eventuell vorhanden Generic-Typen
     */
    public String toString(String indent) {
        String s = indent+"Declaration: "+typ.content;
        if(genericTypes!=null && genericTypes.size()!=0){
            s+="<";
            int i = 0;
            s+=genericTypes.get(i).content;
            i++;
            while(genericTypes.size()>i){
                s+=",";
                s+=genericTypes.get(i).content;
            }
            s+=">";
        }
        s+=" "+name.content;
        return s;
    }

    /**
     * Die Deklaration wird in die Symboltabelle übernommen
     */
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        tabelle.add(name.content, Type.getType(typ.content), this);
    }

}


/**
 * PrintNode stellt die Methode System.out.prinln() dar
 * Dazu wird ein ExprNode übergeben, der rekursiv ausgewertet wird
 */
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

    /**
     * Auswertung des ExprNode, dessen Ergebnis (Value) ausgegeben wird
     */
    public void run() {
        Value ausg = expr.runExpr();
        System.out.println(ausg.toString());
    }
}


/**
 * Beschreibt ein leeres Statement ohne Inhalt. 
 * Daher ist auch keine runExpr()-Methode enthalten
 */
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
        if (!(leftT.equals(rightT))) {
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
        for (Pair<ExprNode,ExprNode> pair : entries) {
            if(pair.getR() == null) 
            {
                char a = pair.getL().runExpr().c;
                return new Value(new Range(a,a));
            } else {
                return new Value(new Range(pair.getL().runExpr().c,
                    pair.getR().runExpr().c));
            }
        }
        return new Value(new Range());
    }
}

//Transitionnode beinhaltet zwei Konstruktoren:
//Normaler Übergang: StartState -- [Range] --> EndState
//Epsilon-Übergang : StartState ---> EndState
//Bei der runExpr() werden die runExpr des State-& RangeNodes aufgerufen 
//und zwischen EpsilonTransition und normaler Transition unterschieden
class TransitionNode extends ExprNode {
    ExprNode start = null;
    ExprNode end = null;
    ExprNode r = null;

    public TransitionNode(ExprNode start, ExprNode r, ExprNode end) {
        super(start.start, end.end);
        this.start = start;
        this.end = end;
        this.r = r;
    }

    public TransitionNode(ExprNode start, ExprNode end) {
        super(start.start, end.end);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString(String indent) {
        if(r == null){
        return indent + "Transitionnode: $\""+ start.toString() +"\"--->$\"" + end.toString() + "\"";
        } 
        else{
            return indent + "Transitionnode: $\""+ start.toString() +"\"--['" + r.toString() + "'-'"+ r.toString() + "'" + "]-->$\"" + end.toString() + "\"";
        }
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.transitionType;
    }
    public Value runExpr() {
        Value erg;

        if(r == null){
            erg = new Value(new EpsilonTransition(start.runExpr().s, end.runExpr().s));
        } 
        else {
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




class SetNode extends ExprNode {
    Token type;
    List<Pair<ExprNode,ExprNode>> entries;
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
    List<Pair<ExprNode,ExprNode>> entries;

    public MapNode(List<Pair<ExprNode,ExprNode>> entries){
        super(entries.get(0).getL().start,
        ((entries.get(entries.size()-1).getR())!=null) ? entries.get(entries.size()-1).getR().end : entries.get(0).getL().end);
        this.entries = entries;
    }

    @Override
    public String toString(String indent) {
        String ret = indent+"Map: ";
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
        ExprNode typeLeft = entries.get(0).getL();
        ExprNode typeRight = entries.get(0).getR();
        int sizeList = entries.size();
        for(int i = 0; i < sizeList; i++)
        {
            if(entries.get(i).getL().type != typeLeft.type)
            {
                errors.add(new SemanticError(start, end, "Different Type in Map-KeyTypes :" + entries.get(i).getL().type.toString()  + "!=" + typeLeft.type.toString()));
            }
        }

        for(int i = 0; i < sizeList; i++)
        {
            if(entries.get(i).getR().type != typeRight.type)
            {
                errors.add(new SemanticError(start, end, "Different Type in Map-ValueTypes :" + entries.get(i).getR().type.toString()  + "!=" + typeRight.type.toString()));
            }
        }

        Value typeleft = typeLeft.runExpr();
        Value typeright = typeRight.runExpr();
        //Map test = new HashMap<typeleft, typeright>();
        //Value erg = new Value(new HashMap<typeleft, typeright>());
        //erg.type = Type.mapType; 
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

abstract class RegularExpressionNode extends ExprNode {
    RegularExpressionNode left;
    RegularExpressionNode right;

    public RegularExpressionNode(RegularExpressionNode left, RegularExpressionNode right) {
        super(left.start, right.end);
    }
    
}

class OrNode extends RegularExpressionNode {
    RegularExpressionNode left;
    RegularExpressionNode right;
    
    public OrNode(RegularExpressionNode left, RegularExpressionNode right) {
        super(left, right);
        this.left = left;
        this.right = right;
    }
    @Override
    public String toString(String indent) {
        return indent + "RE-Or:" + left.start.content + "|" + right.end.content;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.orType;
    }

    @Override
    public Value runExpr() {
        Value erg = new Value();
        Value orLeft = left.runExpr();
        Value orRight = right.runExpr();
        erg.re = new Or(orLeft.re, orRight.re);
        return erg;
    }
}

class ConcatNode extends RegularExpressionNode {
    RegularExpressionNode left;
    RegularExpressionNode right;


    public ConcatNode(RegularExpressionNode left, RegularExpressionNode right) {
        super(left, right);
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString(String indent) {
        return indent + "RE-Concat:" + left.start.content + right.end.content;
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.concatType;
    }

    @Override
    public Value runExpr() {
        Value erg = new Value();
        Value orLeft = left.runExpr();
        Value orRight = right.runExpr();
        erg.re = new Concat(orLeft.re, orRight.re);
        return erg;
    }

}

class StarNode extends RegularExpressionNode {
    RegularExpressionNode regEx;

    public StarNode(RegularExpressionNode regEx) {
        super(regEx, regEx);
        this.regEx = regEx;
    }

    @Override
    public String toString(String indent) {
        return indent + "RE-Star:" + regEx.toString() + "*";
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.starType;
    }

    @Override
    public Value runExpr() {
        Value erg = new Value();
        Value rest = regEx.runExpr();
        erg.re = new Star(rest.re);
        return erg;
    }

}



class RangeExprNode extends RegularExpressionNode {
    RangeNode ra;

    public RangeExprNode(RangeNode ra) {
        super(null,null);
        this.ra = ra;
    }
    @Override
    public String toString(String indent) {
        return indent + "RE-RangeExpr: new Range(" + ra.toString() + ")";
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.rangeExprType;
    }

    @Override
    public Value runExpr() {
        Value erg = new Value();
        erg.rExpr = new RangeExpr(ra.runExpr().r);
        return erg;
    }
}

class EmptyWordNode extends RegularExpressionNode {

    public EmptyWordNode() {
        super(null,null);
    }
    @Override
    public String toString(String indent) {
        
        return "EmptyWord";
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.emptyWordType;
    }

    @Override
    public Value runExpr() {
        Value erg = new Value();
        erg.rExpr = null;
        return erg;
    }
    
}