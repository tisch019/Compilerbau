import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Lexer {
    ArrayList<Integer> buffer = new ArrayList<>();
    int marked = -1;
    int current;
    int start;
    boolean ended = false;
    InputStream input = null;

    public void close() throws IOException {
        if (input != null)
            input.close();
        input = null;
    }

    boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    boolean isIdentChar(int c) {
        if (c >= 'a' && c <= 'z')
            return true;
        if (c >= 'A' && c <= 'Z')
            return true;
        if (c == '_')
            return true;
        return false;
    }

    public Token getNextToken() throws IOException {
        marked = -1;
        int state = 0;
        Token t = new Token();
        do {
            int nextChar = getNextChar();
            switch (state) {
                case 0:
                    switch (nextChar) {
                        case '(':
                            mark();
                            t.kind = Token.Type.BRACKETSTART;
                            state = 100;
                            break;
                        case ')':
                            mark();
                            t.kind = Token.Type.BRACKETSTART;
                            state = 100;
                            break;
                        case '{':
                            mark();
                            t.kind = Token.Type.BLOCKSTART;
                            state = 100;
                            break;
                        case '}':
                            mark();
                            t.kind = Token.Type.BLOCKEND;
                            state = 100;
                            break;
                        case ';':
                            mark();
                            t.kind = Token.Type.SEM;
                            state = 100;
                            break;
                        case ' ':
                        case '\t':
                        case '\n':
                        case '\r':
                            mark();
                            t.kind = Token.Type.WS;
                            state = 100;
                            break;
                        case '+':
                        case '-':
                            mark();
                            t.kind = Token.Type.POP;
                            state = 100;
                            break;
                        case '%':
                        case '*':
                        case '/':
                            mark();
                            t.kind = Token.Type.LOP;
                            state = 100;
                            break;
                        case '$':
                            mark();
                            t.kind = Token.Type.STATE;
                            state = 5;
                            break;
                        case '<':
                        case '>':
                        case '!':
                            mark();
                            t.kind = Token.Type.COMP;
                            state = 1;
                            break;
                        case '=':
                            mark();
                            t.kind = Token.Type.SETTO;
                            state = 2;
                            break;
                        case 'i':
                            mark();
                            t.kind = Token.Type.IFSTART;
                            state = 9;
                            break;
                        case -1:
                            mark();
                            t.kind = Token.Type.EOF;
                            state = 100;
                            break;
                        default:
                            if (isIdentChar(nextChar)) {
                                mark();
                                t.kind = Token.Type.IDENTIFIER;
                                state = 3;
                            } else if (isDigit(nextChar)) {
                                mark();
                                t.kind = Token.Type.INT;
                                state = 4;
                            } else
                                state = 100;
                    }
                    break;
                // Case 1 COMPARE
                case 1:
                    if (nextChar == '=')
                        mark();
                    state = 100;
                    break;
                // Case 2 COMPARE
                case 2:
                    if (nextChar == '=') {
                        mark();
                        t.kind = Token.Type.COMP;
                    }
                    state = 100;
                    break;
                // Case 3 IDENTIFIER
                case 3:
                    if (isIdentChar(nextChar))
                        mark();
                    else
                        state = 100;
                    break;
                case 4:
                    if (isDigit(nextChar))
                        mark();
                    else
                        state = 100;
                    break;
                // Case 5 - 8 STATE
                case 5:
                    if (nextChar == '"') {
                        mark();
                        state = 8;
                    } else {
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                case 6:
                    if (isIdentChar(nextChar)) {
                        mark();
                        state = 9;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                case 7:
                    if (isIdentChar(nextChar) || isDigit(nextChar))
                        mark();
                    else
                        state = 10;
                    break;
                case 8:
                    if (nextChar == '"') {
                        mark();
                        state = 100;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                // Case 9 - 10 IFSTART
                case 9:
                    if (nextChar == 'f') {
                        mark();
                        state = 10;
                    } else if (isIdentChar(nextChar) || isDigit(nextChar)) {
                        mark();
                        t.kind = Token.Type.IDENTIFIER;
                        state = 3;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                case 10:
                    if (nextChar == '(') {
                        mark();
                        state = 100;
                    } else {
                        mark();
                        t.kind = Token.Type.IDENTIFIER;
                        state = 3;
                    }
                    break;
            }
        } while (state != 100);
        if (marked == -1) {
            t.kind = Token.Type.ERROR;
            marked = start;
        }
        t.content = proceed();
        return t;
    }

    // input side
    public int getNextChar() throws IOException {
        if (current >= buffer.size())
            if (ended)
                return -1;
            else
                buffer.add(input.read());

        return buffer.get(current++);
    }

    public void mark() {
        marked = current - 1;
    }

    public void reset() {
        current = start;
    }

    public String proceed() {
        String erg = "";
        for (; start <= marked; start++)
            erg += (char) (int) buffer.get(start);
        reset();
        return erg;
    }

    public Lexer(String name) throws IOException {
        input = new FileInputStream(name);
    }
}
