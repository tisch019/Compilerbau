import java.util.LinkedList;
import java.util.List;

public abstract class Node {
    Token start, end;

    public Node(Token start, Token end) {
        this.end = end;
        this.start = start;
    }
    public abstract String toString(String indent);

    //public void semantischeAnalyse(SymbolTabelle tabelle, List<CompilerError> errors) {}

    public void run() {}
}


