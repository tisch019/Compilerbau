public class InterpreterError extends Exception {
    String message;
    public InterpreterError(String msg) {
        message = msg;
    }
    public String toString(){
        return message;
    }
}

class LexerError extends InterpreterError {
    Token token;
    public LexerError(Token t, String msg) {
        super(msg);
        token = t;
    }
    public String toString(){
        return "line "+token.line+", column "+token.column+":  "+super.toString();
    }
}

class ParserError extends InterpreterError {
    Token token;

    public ParserError(Token ts, String msg) {
        super(msg);
        token = ts;
    }
    public String toString(){
        return "line "+token.line+", column "+token.column+":  "+super.toString();
    }
}

class SemanticError extends InterpreterError {
    Token start;
    Token end;

    public SemanticError(Token ts, Token te, String msg) {
        super(msg);
        start = ts;
        end= te;
    }
    public String toString(){
        return "line "+start.line+", column "+start.column+":  "+super.toString();
    }
}
