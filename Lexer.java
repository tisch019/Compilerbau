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
                            t.kind = Token.Type.BRACKETEND;
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
                            state = 20;
                            break;
                        case '[':
                            mark();
                            t.kind = Token.Type.RANGESTART;
                            state = 12;
                            break;
                        case ']':
                            mark();
                            t.kind = Token.Type.RANGEEND;
                            state = 14;
                            break;
                        case ';':
                            mark();
                            t.kind = Token.Type.SEM;
                            state = 100;
                            break;
                        case ',':
                            mark();
                            t.kind = Token.Type.COMMA;
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
                            mark();
                            t.kind = Token.Type.POP;
                            state = 100;
                            break;
                        case '-':
                            mark();
                            t.kind = Token.Type.POP;
                            state = 17;
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
                        case '!':
                            mark();
                            t.kind = Token.Type.BOOLNEG;
                            state = 1;
                            break;
                        case '<':
                            mark();
                            t.kind = Token.Type.COMP;
                            state = 1;
                            break;
                        case '>':
                            mark();
                            t.kind = Token.Type.COMP;
                            state = 1;
                            break;
                        case '=':
                            mark();
                            t.kind = Token.Type.SETTO;
                            state = 2;
                            break;
                        case '"':
                            mark();
                            t.kind = Token.Type.STRING;
                            state = 21;
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
                    if (nextChar == '=') {
                        mark();
                        t.kind = Token.Type.COMP;
                    } else if (nextChar == '$') {
                        t.kind = Token.Type.FASTART;
                    }
                    state = 100;
                    break;
                // Case 2 SETTO COMPARE
                case 2:
                    if (nextChar == '=') {
                        mark();
                        t.kind = Token.Type.COMP;
                    }
                    state = 100;
                    break;
                // Case 3 IDENTIFIER
                case 3:
                    if (isIdentChar(nextChar)) {
                        mark();
                    } else {
                        state = 100;
                    }
                    break;
                // Case 4 INT
                case 4:
                    if (isDigit(nextChar)) {
                        mark();
                    }
                    else if (nextChar == '.') {
                        state = 15; // Check digits
                    }
                    else {
                        state = 100;
                    }
                    break;
                // Case 5 - 10 STATE and STATEACC
                case 5:
                    if (nextChar == '"') {
                        mark();
                        state = 6;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                case 6:
                    if (isIdentChar(nextChar)) {
                        mark();
                        state = 7;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                case 7:
                    if (isIdentChar(nextChar) || isDigit(nextChar)) {
                        mark();
                    }
                    else {
                        state = 8;
                    }
                    break;
                case 8:
                    if (nextChar == '"') {
                        mark();
                        state = 9;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                case 9:
                    if (nextChar == '^') {
                        mark();
                        state = 10;
                        t.kind = Token.Type.STATEACC;
                    } else {
                        state = 100;
                    }
                    break;
                case 10:
                    if (nextChar == '1') {
                        mark();
                        state = 100;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                // Case 12 - 13 ARRAYSTART MAPSTART
                case 12:
                    if (nextChar == '[') {
                        mark();
                        t.kind = Token.Type.ARRAYSTART;
                        state = 13;
                    } else {
                        state = 100;
                    }
                    break;
                case 13:
                    if (nextChar == '"') {
                        t.kind = Token.Type.MAPSTART;
                    }
                    state = 100;
                    break;
                // Case 14 MAPEND ARRAYEND
                case 14:
                    if (nextChar == ']') {
                        mark();
                        t.kind = Token.Type.TRANSEND; //MAPEND oder ARRAYEND Wie unterscheiden?
                        state = 10;
                    } else {
                        state = 100;
                    }
                    break;
                // Case 15 -16 DOUBLE
                case 15:
                    if (isDigit(nextChar)) {
                        mark();
                        t.kind = Token.Type.DOUBLE;
                        state = 16;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                case 16:
                    if (isDigit(nextChar)) {
                        mark();
                    } else {
                        state = 100;
                    }
                    break;
                // Case 17 - 19 TRANS
                case 17:
                    if (nextChar == '-') {
                        mark();
                        t.kind = Token.Type.TRANSSTART;
                        state = 18;
                    } else {
                        state = 100;
                    }
                    break;
                case 18:
                    if (nextChar == '>') {
                        mark();
                        t.kind = Token.Type.TRANSEND;
                        state = 100;
                    } else if (nextChar == '-'){
                        mark();
                        state = 19;
                    } else {
                        state = 100;
                    }
                    break;
                case 19:
                    if (nextChar == '>') {
                        mark();
                        t.kind = Token.Type.TRANSEPSI;
                    }
                    // --- Wird auch als TRANSSTART erkannt
                    state = 100;
                    break;
                // Case 20 FAEND
                case 20:
                    if (nextChar == '>') {
                        mark();
                        t.kind = Token.Type.FAEND;
                    }
                    state = 100;
                    break;
                // Case 21 STRING
                case 21:
                    if (nextChar == '"') {
                        mark();
                        state = 100;
                    } else {
                        mark();
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
