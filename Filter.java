import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;

class Filter{
    Logger logger = Logger.getLogger(Filter.class.getName());
    int current;
    List<InterpreterError> errors;
    LinkedList<Token> tokenlist = new LinkedList<>();
    Lexer lexer;
    int index;
    int line = 1;
    int column = 1;

    public Filter(Lexer lexer, List errors) {
        logger.setLevel(Level.INFO);
        this.lexer = lexer;
        this.errors = errors;
        logger.info("Filter created");
    }

    void fillBuffer(int n) throws IOException {
        for (int i=0; i<n;i++) tokenlist.add(getNextFilteredToken());
    }

    public void matchToken(Token.Type type, Set<Token.Type> sync) throws IOException, ParserError{
        if (getToken().kind == type) matchToken();
        else {
            Token currentToken = getToken();
            ParserError error = new ParserError(currentToken, type+" expected instead of "+currentToken.kind);
            errors.add(error);
            while (!sync.contains(getToken().kind)) matchToken();
            throw error;
        }
    }

    Token getNextFilteredToken() throws IOException {
        Token currentToken;
        int help = 1;
        do {
            currentToken = lexer.getNextToken();
            currentToken.line = line;
            currentToken.column = column;
            if (currentToken.content.equals("\n") || currentToken.content.equals("\r")) {
                line = line + (help%2);
                column = 1;
                help++;
            }
            else column += currentToken.content.length();
            if (currentToken.kind == Token.Type.ERROR) errors.add(new LexerError(currentToken, "Unknown token " + currentToken.content));
        } while (currentToken.kind == Token.Type.WS || currentToken.kind == Token.Type.ERROR);
        if (currentToken.kind == Token.Type.IDENTIFIER) {
            logger.info("Content of current token from kind identifier: " + currentToken.content);
            switch (currentToken.content) {
                case "String": currentToken.kind = Token.Type.KEYSTRING; break;
                case "char": currentToken.kind = Token.Type.KEYCHAR; break;
                case "int": currentToken.kind = Token.Type.KEYINT; break;
                case "boolean": currentToken.kind = Token.Type.KEYBOOL; break;
                case "false":
                case "true": currentToken.kind = Token.Type.BOOL; break;
                case "if": currentToken.kind = Token.Type.IFSTART; break;
                case "else": currentToken.kind = Token.Type.ELSE; break;
                case "while": currentToken.kind = Token.Type.WHILE; break;
                case "Range": currentToken.kind = Token.Type.KEYRANGE; break;
                case "State": currentToken.kind = Token.Type.KEYSTATE; break;
                case "Transition": currentToken.kind = Token.Type.KEYTRANSITION; break;
                case "FA": currentToken.kind = Token.Type.KEYFA; break;
                case "RA": currentToken.kind = Token.Type.KEYRA; break;
                case "Set": currentToken.kind = Token.Type.KEYSET; break;
                case "Map": currentToken.kind = Token.Type.KEYMAP; break;
                case "print": currentToken.kind = Token.Type.PRINT; break;
                default: currentToken.kind = Token.Type.IDENTIFIER; break;
            }
        } else if (currentToken.kind == Token.Type.CONTAINSHELP){
                switch (currentToken.content) {
                    case ".contains": currentToken.kind = Token.Type.CONTAINS; break;
                    case ".containsKey": currentToken.kind = Token.Type.CONTAINSKEY; break;
                    default: currentToken.kind = Token.Type.ERROR; logger.info("Error in Filter in CONTAINSHELPER with content: " + currentToken.content); break;
                }
        }
        logger.info("Recognized type of current token: " + currentToken.kind);
        currentToken.index = index++;
        return currentToken;
    }

    public Token getToken(int i) throws IOException{
        fillBuffer(current+i+1 - tokenlist.size());
        return tokenlist.get(current+i);
    }

    public Token getToken() throws IOException {
        return getToken(0);
    }
    public void matchToken() {
        current++;
    }
}