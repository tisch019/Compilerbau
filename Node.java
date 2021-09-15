import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.type.ErrorType;

import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;


/*Abstrakte Klassen für alle folgenden Node-Typen zur Erstellung des AST
//Die abstrakte Klasse Node ist die Oberklasse aller Knotenpunkte im AST
*/
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
        if(genericTypes == null)
            this.genericTypes = new ArrayList<>();
        else
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
        if(genericTypes.size() > 0){
            Type newT = Type.getType(typ.content).copy();
            for(int i = 0; i < genericTypes.size() ;i++){
                newT.addGenTyp(Type.getType(genericTypes.get(i).content));
            }
            tabelle.add(name.content, newT, this);
        }else
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


/**
 * Besteht aus drei Nodes:
 * Ifnode: if(expr) {stmnt} else {elseStmnt}
 */
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

    /**
     * Semantische Analyse: Prüfung auf boolean in der Expression (expr) für die if-Anweisung
     * und Analyse des else-Statements, wenn vorhanden
     */
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        if (expr.semantischeAnalyseExpr(tabelle,errors) != Type.booleanType)
        {
            errors.add(new SemanticError(start,end,"if-expression must be boolean"));
        }
        stmnt.semantischeAnalyse(tabelle, errors);
        if (elseStmnt!=null) elseStmnt.semantischeAnalyse(tabelle, errors);
    }

    /**
     * Auswertung der Expression und eventuelle Auswertung des Statements oder 
     * andernfalls des else-Statements, falls vorhanden
     */
    public void run() {
        Value bed = expr.runExpr();
        if (bed.b) stmnt.run();
        else if (elseStmnt!=null) elseStmnt.run();
    }
}


/**
 * Enthält zwei Nodes:
 * WhileNode: while(expr) {stmnt}
 */
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

    /**
     * Analyse, ob Expression-Node (expr) vom Typ Boolean ist, wenn true, dann Analyse des Statement-Nodes,
     * Andernfalls Hinzufügen eines semantischen Fehlers
     */
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        if (expr.semantischeAnalyseExpr(tabelle, errors) != Type.booleanType)
        {
            errors.add(new SemanticError(start,end,"while-condition must be boolean"));
        }
        stmnt.semantischeAnalyse(tabelle, errors);
    }

    /**
     * Auswertung der Expression und eventuelle Auswertung des StatementNodes
     */
    public void run() {
        Value bed = expr.runExpr();
        while (bed.b) stmnt.run();
    }
}


/**
 * Beschreibung eines Blocks in der Skriptsprache durch eine
 * Liste an Nodes (Deklarationen und Statements)
 */
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

    /**
     * Erzeugen einer lokalen Symboltabelle mit Aufruf der 
     * semantischen Analyse der einzelnen Nodes der Liste
     */
    public void semantischeAnalyse(SymbolTabelle tabelle, List<InterpreterError> errors) {
        SymbolTabelle lokal = new SymbolTabelle(tabelle);
        for (Node node: declAndStmnts) node.semantischeAnalyse(lokal, errors);
    }

    /**
     * Aufruf der run-Methode jedes einzelnen Nodes in der Liste
     */
    public void run() {
        for (Node node: declAndStmnts) node.run();
    }
}

/**
 * Erweiterung eines Statement-Node durch einen ExprNode
 */
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


/**
 * NumberINode als Repräsentation eines Integers
 */
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
    /**
     * Umwandlung des Inhalts vom Token in einen
     * Integer (Abspeicherung im Value)
     */
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

/**
 * Node für Operationen (op) zweier ExprNodes
 */
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

    /**
     * Überprüfung, ob übergebene ExprNodes miteinader operieren dürfen
     */
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        Type leftT = left.semantischeAnalyseExpr(tabelle, errors);
        Type rightT = right.semantischeAnalyseExpr(tabelle, errors);
        Type res;
        if (!(leftT.equals(rightT))) {
            if (leftT == Type.kgT(leftT, rightT))
                res = leftT;
            else {
                errors.add(new SemanticError(start, end, "Incompatible Types for BinOp "+ leftT.name + " : " + rightT.name));
                res = Type.errorType;
            }
        }
        if (op.kind == Token.Type.COMP) res = type = Type.booleanType;
        else res = type = Type.kgT(leftT, rightT);
        return res;
    }

    /**
     * Ausführen der Operation (op)
     * auf die zwei ExprNodes
     */
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
                if (type == Type.charType){
                    if(right.type == Type.intType)
                        erg.c = (char) (leftV.c + rightV.i);
                }
                if(type == Type.finiteAutomataType){
                    if(right.type == Type.transitionType){
                        erg.fa = leftV.fa.addTransitions(rightV.t);
                    }else if(right.type == Type.finiteAutomataType){
                        erg.fa = FiniteAutomata.union(new State("A"),leftV.fa,rightV.fa);
                    }
                }
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
                    break;
                case "%": if (type == Type.intType)
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

/**
 * Operationen, die auf einem ExprNode ausgeführt werden (-, ! für Negation)
 */
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
            case "++":
                if (erg.type == Type.intType)
                    erg.i = ++leftV.i;
                    ((IdentifierNode)kid).runSetExpr(erg).copy();
                    break;
            case "--":
                if (erg.type == Type.intType)
                    erg.i = --leftV.i;
                    ((IdentifierNode)kid).runSetExpr(erg).copy();
                    break;
            case "!": if (erg.type == Type.booleanType)
                erg.b = !leftV.b;
                break;
        }
        return erg;
    }
}

/**
 * Knoten eines einzelnen Char-Zeichens, welcher in einem Token enthalten ist
 */
class CharNode extends ExprNode {
    Token content;

    public CharNode(Token content) {
        super(content, content);
        this.content = content;
    }

    /**
     * Ausgabe des char-Zeichens
     */
    public String toString(String indent) {
        return indent+"Char: "+ content.content;
    }

    /**
     * Rückgabe des Typs charType in der semantischen Analyse, 
     * da ein Zeichen nicht weiter zerlegt werden kann
     */
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return type = Type.charType;
    }

    /**
     * Erzeugung eines Value mit dem Inhalt des Tokens content an der Stelle 0 
     */
    public Value runExpr() {
        Value erg = new Value(); erg.type = Type.charType;
        erg.c = content.content.charAt(0);
        return erg;
    }
}

/**
 * Identisch zum Char-Node, 
 * wobei in der runExpr der gesamte Inhalt des Tokens content in ein Value gepackt wird
 * mit dem Typ stringType
 */
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

/**
 * Identisch zum CharNode, wobei in der runExpr 
 * der Inhalt des Tokens content in ein Boolean geparst wird
 * mit dem Typ booleanType
 */
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

/*StateNode für den endlichen Automaten
In der runExpr wird mit einem Boolean-Flag geprüft, 
ob der State akzeptierend ist oder nicht
*/
class StateNode extends ExprNode{
    boolean accept = false;
    Token content;

    /**
     * Konstruktor des StateNodes
     * @param content Name des States
     * @param accept Akzeptierend (1) oder nicht (0)
     */
    public StateNode(Token content, boolean accept){
        super(content,content);
        this.content = content;
        this.accept = accept;
    }
    public String toString(String indent){
        if(accept) {
        return indent+"State: "+content.content + "(akzeptierend)";
        } else {
            return indent+"State: "+content.content + "(nicht akzeptierend)";
        }
    }

    /**
     * Rückgabe des Typs stateType
     */
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors){
        return type = Type.stateType;
    }

    /**
     * Erzeugung eines State-Objektes in einem Value-Objekt
     * @return Value-Objekt mit State-Objekt als Inhalt
     */
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

/**
 * RangeNode enthält eine Liste aus Paaren (Pairs), die vom Typ ExprNode sind.
 */
class RangeNode extends ExprNode{
    List<Pair<ExprNode,ExprNode>> entries;

    /**
     * Konstruktor des RangeNodes
     * @param entries Liste der Pairs
     * Prüfung, ob zweites Teil-Paar leer ist
     */
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

    /**
     * Prüfung, ob alle Pairs der Liste vom Typ charType,
     * sonst Error-Meldung
     */
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

    /**
     * Erzeugung eines Range für jedes Pair
     */
    public Value runExpr() {
        for (Pair<ExprNode,ExprNode> pair : entries) {
            
            if(pair.getR() == null) 
            {
                //rechter Teil des Pair ist leer
                char a = pair.getL().runExpr().c;
                return new Value(new Range(a,a));
            } else {
                //rechter Teil enthält einen Char
                return new Value(new Range(pair.getL().runExpr().c,
                    pair.getR().runExpr().c));
            }
        }
        return new Value(new Range());
    }
}

/**Transitionnode beinhaltet zwei Konstruktoren:
 * Normaler Übergang: StartState -- [Range] --> EndState
 * Epsilon-Übergang : StartState ---> EndState
 * Bei der runExpr() werden die runExpr des State-& RangeNodes aufgerufen 
 * und zwischen EpsilonTransition und normaler Transition unterschieden
*/
class TransitionNode extends ExprNode {
    ExprNode start = null;
    ExprNode end = null;
    ExprNode r = null;

     /**
     * Normaler Übergang
     */
    public TransitionNode(ExprNode start, ExprNode r, ExprNode end) {
        super(start.start, end.end);
        this.start = start;
        this.end = end;
        this.r = r;
    }

     /**
     * Epsilon-Übergang
     */
    public TransitionNode(ExprNode start, ExprNode end) {
        super(start.start, end.end);
        this.start = start;
        this.end = end;
    }

    /**
     * Ausgabe des jeweiligen Inhalts
     */
    @Override
    public String toString(String indent) {
        if(r == null){
        return indent + "Transitionnode: $\""+ start.toString() +"\"--->$\"" + end.toString() + "\"";
        } 
        else{
            return indent + "Transitionnode: $\""+ start.toString() +"\"--['" + r.toString() + "'-'"+ r.toString() + "'" + "]-->$\"" + end.toString() + "\"";
        }
    }

     /**
     * Rückgabe des Typs transitionType, da Reihenfolge und Typen der Token an dieser Stelle bereits korrekt
     */
    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        if(start.semantischeAnalyseExpr(tabelle, errors) != Type.stateType)
            errors.add(new SemanticError(start.start, start.end, "transition start needs to be a State"));
        if(end.semantischeAnalyseExpr(tabelle, errors) != Type.stateType)
            errors.add(new SemanticError(end.start, end.end, "transition end needs to be a State"));
        if(r != null)
            if(r.semantischeAnalyseExpr(tabelle, errors) != Type.rangeType)
                errors.add(new SemanticError(start.start, start.end, "transition range needs to be a Range"));
        return Type.transitionType;
    }

    /**
     * Erzeugung des (Epsilon) Transitionsobjekts
     * @return Value-Objekt mit Transition als Inhalt
     */
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

/**
 * FiniteAutomataNode gibt einen FiniteAutomata mit einem StartState wieder.
 * Transitionen werden über den Parser an den FiniteAutomata gebunden
*/
class FiniteAutomataNode extends ExprNode{
    ExprNode start;
    ExprNode transitionSet;
    RegularExpressionNode regex;
    boolean regexFA;

    /**
     * Konstruktor endlicher Automat
     * @param start StateNode als Start erforderlich
     */
    public FiniteAutomataNode(ExprNode start, ExprNode transitionSet) {
        super(start.start, transitionSet.end);
        this.start = start;
        this.transitionSet = transitionSet;
        regexFA = false;
    }
    public FiniteAutomataNode(RegularExpressionNode regex){
        super(regex.start,regex.end);
        this.regex = regex;
        regexFA = true;
    }

    @Override
    public String toString(String indent) {
        if(regexFA)
            return indent + "FiniteAutomataNode: "+regex.toString();
        else
            return indent + "FiniteAutomataNode: <$\"" + start + ","+transitionSet+" >";
    }

     
    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        if(!regexFA){
            if(start.semantischeAnalyseExpr(tabelle, errors) != Type.stateType)
                errors.add(new SemanticError(start.start, start.start, "FA needs a State"));
            Type setT = Type.setType.copy();
            setT.addGenTyp(Type.transitionType);
            if(transitionSet.semantischeAnalyseExpr(tabelle, errors) != setT);
        }

        return Type.finiteAutomataType;
    }

    /**
     * Erzeugung des FiniteAutomata-Objekts
     * Unterscheidung, ob es sich um einen vordefinierten Automaten
     * oder eine regulären Ausdruck handelt (regexFA)
     */
    @Override
    public Value runExpr() {
        Value erg;
        //endlicher Automat aus regulärem Ausdruck
        if(regexFA){
            Value ra = new Value(regex.runExpr());
            erg = new Value(ra.re.berrySethi(1));
        }else{
            //vordefinierter Automat
            FiniteAutomata fa = new FiniteAutomata(start.runExpr().s);
            Set<Value> transitions = transitionSet.runExpr().st;
            if(transitions.size() != 0){
                for(Value trans : transitions){
                    fa.addTransitions(trans.t);
                }
            }
            erg = new Value(fa);
        }
        return erg;
    }

}

/**
 * SetNode entspricht Set aus der Java-Bibliothek
 * Inhalt des Sets ist die Liste von ExprNodes
 */
class SetNode extends ExprNode {
    List<ExprNode> list;
    
    
    public SetNode(Token start,List<ExprNode> list){
        super(start, start);
        this.list = list;
    }

    @Override
    public String toString(String indent) {
        return indent + "Set<" + list.get(0).type + "> = new HashSet<" + list.get(0).type + ">;" ;
    }

    /**
     * Typüberprüfung, ob alle ExprNodes in der Liste
     * den gleichen Typ besitzten
     */
    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        if(list.size() == 0)
            return Type.setType;

        Type setType = list.get(0).semantischeAnalyseExpr(tabelle, errors);
        
        for(int i = 0; i < list.size(); i++)
        {
            //Prüfung der Typen in der Liste
            if(list.get(i).semantischeAnalyseExpr(tabelle, errors) != setType )
            {
                errors.add(new SemanticError(start, end, "Different Type in Set: received "+ list.get(i).type + " but expected " + setType));
                return Type.errorType;
            }
        }
        Type res = Type.setType.copy();
        res.addGenTyp(setType);
        return res;
    }

    /**
     * Anlegen einer HashSet mit Aufruf der runExpr() 
     * der einzelnen ExprNodes in der Liste und Aufnahme dessen Ergebnis
     * in das HashSet
     */
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

/**
 * MapNode entspricht Map aus der Java-Bibliothek
 * Enthält eine Liste an Paaren vom Typ ExprNodes
 */
class MapNode extends ExprNode {
    //Stets gleiche Anzahl an Keys und Values in der Map
    List<Pair<ExprNode,ExprNode>> entries;

    public MapNode(Token start,List<Pair<ExprNode,ExprNode>> entries){
        super(start,start);
        this.entries = entries;
    }

    @Override
    public String toString(String indent) {
        String ret = indent+"Map: ";
        for (Pair<ExprNode,ExprNode> pair : entries) {
            
            ret += "'"+pair.getL()+"'-'"+pair.getR()+"' ";
        }
        return  ret;
    }

    /**
     * Prüfung auf Typ-Gleichheit der Keys und Values 
     * aus der Liste mit Paaren
     */
    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        if(entries.size() == 0)
            return Type.mapType;
        
        //Key-Typ der Map
        Type typeLeft = entries.get(0).getL().semantischeAnalyseExpr(tabelle, errors);
        //Value-Typ der Map
        Type typeRight = entries.get(0).getR().semantischeAnalyseExpr(tabelle, errors);
        //Anzahl Paare in der Liste
        int sizeList = entries.size();

        for(int i = 0; i < sizeList; i++)
        {
            if(entries.get(i).getL().semantischeAnalyseExpr(tabelle, errors) != typeLeft)
            {
                errors.add(new SemanticError(start, end, "Different Type in Map-KeyTypes :" + entries.get(i).getL().type.toString()  + "!=" + typeLeft.toString()));
            }
        }

        for(int i = 0; i < sizeList; i++)
        {
            if(entries.get(i).getR().semantischeAnalyseExpr(tabelle, errors) != typeRight)
            {
                errors.add(new SemanticError(start, end, "Different Type in Map-ValueTypes :" + entries.get(i).getR().type.toString()  + "!=" + typeRight.toString()));
            }
        }

        //Kopieren des Typs als Name (siehe Klasse Type)
        Type erg = Type.mapType.copy();
        //Hinzufügen des Key- und Value-Typs als Generics (siehe Klasse Type)
        erg.addGenTyp(typeLeft);
        erg.addGenTyp(typeRight); 
        return erg;
    }

    /**
     * Erzeugen der HashMap
     */
    @Override
    public Value runExpr() {
        Map<Value, Value> verified = new HashMap<Value, Value>();
        int sizeList = entries.size();
        for(int i = 0; i < sizeList; i++)
        {
            Value key = entries.get(i).getL().runExpr();
            Value valuetype = entries.get(i).getR().runExpr();
            verified.put(key, valuetype);
        }
        Value erg = new Value(verified);    
        return erg;
    }

}

/**
 * Identisch zu SetNode, wobei keine Typüberprüfung
 * im ArrayNode erfolgt
 */
class ArrayNode extends ExprNode {
    List<ExprNode> list;
    
    
    public ArrayNode(Token start, List<ExprNode> list){
        super(start,start);
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

/**
 * Abstrakter Node für einen regulären Ausdruck
 * bestehend aus zwei Token
 */
abstract class RegularExpressionNode extends ExprNode {
    Token left;
    Token right;

    public RegularExpressionNode(Token left, Token right) {
        super(left, right);
    }
}

/**
 * OrNode: left | right
 */
class OrNode extends RegularExpressionNode {
    RegularExpressionNode left;
    RegularExpressionNode right;
    
    public OrNode(RegularExpressionNode left, RegularExpressionNode right) {
        super(left.start, right.end);
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

    /**
     * Erzeugen eines Or-Objekts
     * Rekursive Auswertung der regular Expressions (left, right)
     */
    @Override
    public Value runExpr() {
        Value erg = new Value();
        Value orLeft = left.runExpr();
        Value orRight = right.runExpr();
        erg.re = new Or(orLeft.re, orRight.re);
        return erg;
    }
}


/**
 * Concat-Node: left ○ right
 */
class ConcatNode extends RegularExpressionNode {
    RegularExpressionNode left;
    RegularExpressionNode right;


    public ConcatNode(RegularExpressionNode left, RegularExpressionNode right) {
        super(left.start, right.end);
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

    /**
     * Erzeugen eines Concat-Objekts im Value-Objekt
     * Rekursive Auswertung der regulären Ausdrücke (left, right)
     */
    @Override
    public Value runExpr() {
        Value erg = new Value();
        Value orLeft = left.runExpr();
        Value orRight = right.runExpr();
        erg.re = new Concat(orLeft.re, orRight.re);
        return erg;
    }

}

/**
 * StarNode: (regEx)*
 */
class StarNode extends RegularExpressionNode {
    RegularExpressionNode regEx;

    public StarNode(RegularExpressionNode regEx) {
        super(regEx.start, regEx.end);
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

    /**
     * Erzeugen eines Star-Objekts im Value-Objekt
     * Rekursive Auswertung des regulären Ausdrucks (regEx)
     */
    @Override
    public Value runExpr() {
        Value erg = new Value();
        Value rest = regEx.runExpr();
        erg.re = new Star(rest.re);
        return erg;
    }

}

/**
 * RangeExprNode: new Range(left, right)
 */
class RangeExprNode extends RegularExpressionNode {
    Token left;
    Token right;

    public RangeExprNode(Token left, Token right) {
        super(left, right);
        this.left = left;
        this.right = right;        
    }
    @Override
    public String toString(String indent) {

            if(left != right) {
               return indent + "RE-RangeExpr: new Range(" + left.content + "," + right.content +")" + "\n";
            } else {
               return indent + "RE-RangeExpr: new Range(" +left.content + ")" + "\n";
            }
    
    }

    @Override
    public Type semantischeAnalyseExpr(SymbolTabelle tabelle, List<InterpreterError> errors) {
        return Type.rangeExprType;
    }


    @Override
    public Value runExpr() {
        Value erg = new Value();

        //Unterscheidung der Ranges [left] oder [left - right]
        if(left != right) {
            erg.re = new RangeExpr(new Range(left.content.charAt(0), right.content.charAt(0)));
         } else {
            erg.re = new RangeExpr(new Range(left.content.charAt(0)));
         }
        
        return erg;
    }
}

/**
 * Darstellung des leeren Worte
 * ohne Inhalt im Value-Objekt
 */
class EmptyWordNode extends RegularExpressionNode {

    public EmptyWordNode(RegularExpressionNode e) {
        super(e.start, e.start);
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
        erg.re = new EmptyWord();
        return erg;
    }
    
}