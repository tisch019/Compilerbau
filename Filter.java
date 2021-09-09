import java.io.IOException;
import java.util.List;
import java.util.Set;

class Filter{
    int current;
    List<InterpreterError> errors;

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

    public Token getToken(int i) throws IOException{
        //TODO
        return null;
    }

    public Token getToken() throws IOException {
        return getToken(0);
    }
    public void matchToken() {
        current++;
    }
}