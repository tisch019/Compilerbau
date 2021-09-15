import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Lexer {
    Logger logger = Logger.getLogger(Lexer.class.getName());
    ArrayList<Integer> buffer = new ArrayList<>();
    int marked = -1;
    int current;
    int start;
    int startOffset;
    boolean firstInRegex;
    boolean lastInRegex;
    boolean inRegex;
    boolean inFa;
    boolean ended = false;
    InputStream input = null;

    public Lexer(String name) throws IOException {
        logger.setLevel(Level.INFO);
        input = new FileInputStream(name);
        logger.info("Lexer created");
        inRegex = false;
        inFa = false;
        firstInRegex = false;
        lastInRegex = false;
    }

    public void close() throws IOException {
        if (input != null)
            input.close();
        input = null;
    }

    boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    boolean isChar(int c) {
        if (c >= 'a' && c <= 'z')
            return true;
        if (c >= 'A' && c <= 'Z')
            return true;
        return false;
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
        startOffset = 0;
        Token t = new Token();
        if (inRegex) {
            state = 50;
        }
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
                            state = 100;
                            break;
                        case '[':
                            mark();
                            t.kind = Token.Type.SQUAREBRACKETOPEN;
                            state = 12;
                            break;
                        case ']':
                            mark();
                            t.kind = Token.Type.SQUAREBRACKETCLOSE;
                            state = 14;
                            break;
                        case ';':
                            mark();
                            t.kind = Token.Type.SEM;
                            state = 100;
                            break;
                        case ':':
                            mark();
                            t.kind = Token.Type.MAPDELI;
                            state = 100;
                            break;
                        case ',':
                            mark();
                            t.kind = Token.Type.COMMA;
                            state = 100;
                            break;
                        case '.':
                            mark();
                            t.kind = Token.Type.CONTAINSHELP;
                            state = 24;
                            break;
                        case ' ':
                        case '\t':
                        case '\n':
                        case '\r':
                            mark();
                            t.kind = Token.Type.WS;
                            state = 100;
                            break;
                        case '^':
                            mark();
                            t.kind = Token.Type.DACH;
                            state = 100;
                            break;
                        case '+':
                            mark();
                            t.kind = Token.Type.POP;
                            state = 29;
                            break;
                        case '-':
                            mark();
                            t.kind = Token.Type.POP;
                            state = 17;
                            break;
                        case '/':
                            mark();
                            t.kind = Token.Type.LOP;
                            state = 25;
                            break;
                        case '%':
                        case '*':
                            mark();
                            t.kind = Token.Type.LOP;
                            state = 100;
                            break;
                        case '$':
                            mark();
                            t.kind = Token.Type.STATE;
                            state = 5;
                            start++;
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
                            if(inFa) {
                                t.kind = Token.Type.FAEND;
                                inFa = false;
                                state = 100;
                            } else {
                                t.kind = Token.Type.COMP;
                                state = 1;
                            }
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
                            start++;
                            break;
                        case '\'':
                            mark();
                            t.kind = Token.Type.CHAR;
                            state = 22;
                            start++;
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
                        inFa = true;
                    } else if (nextChar == '/') {
                        t.kind = Token.Type.FASTART;
                        inRegex = true;
                        firstInRegex = true;
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
                    else {
                        state = 100;
                    }
                    break;
                // Case 5 - 10 STATE and STATEACC
                case 5:
                    if (nextChar == '"') {
                        mark();
                        state = 6;
                        start++;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                        logger.info("Error in Lexer at case 5");
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
                        logger.info("Error in Lexer at case 6");
                    }
                    break;
                case 7:
                    if (isIdentChar(nextChar) || isDigit(nextChar)) {
                        mark();
                    }
                    else if(nextChar == '"'){
                        state = 9;
                        startOffset++;
                    } else {
                        mark();
                        startOffset = 0;
                        t.kind = Token.Type.ERROR;
                        state = 100;
                        logger.info("Error in Lexer at case 9");
                    }
                    break;
                case 9:
                    if (nextChar == '^') {
                        state = 10;
                        t.kind = Token.Type.STATEACC;
                        startOffset++;
                    } else {
                        state = 100;
                    }
                    break;
                case 10:
                    if (nextChar == '1') {
                        startOffset++;
                        state = 100;
                    } else {
                        mark();
                        startOffset = 0;
                        t.kind = Token.Type.ERROR;
                        state = 100;
                        logger.info("Error in Lexer at case 10");
                    }
                    break;
                // Case 12 ARRAYSTART MAPSTART
                case 12:
                    if (nextChar == '[') {
                        mark();
                        t.kind = Token.Type.MAPARRAYSTART;
                    }
                    state = 100;
                    break;
                // Case 14 MAPEND ARRAYEND
                case 14:
                    if (nextChar == ']') {
                        mark();
                        t.kind = Token.Type.MAPARRAYEND;
                    }
                    state = 100;
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
                // Case 21 STRING
                case 21:
                    if (nextChar == '"') {
                        startOffset++;
                        state = 100;
                    } else {
                        mark();
                    }
                    break;
                // Case 22 - 23 CHAR
                case 22:
                    if (isIdentChar(nextChar)) {
                        mark();
                        state = 23;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        start--;
                        state = 100;
                    }
                    break;
                case 23:
                    if (nextChar == '\'') {
                        startOffset++;
                        state = 100;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        state = 100;
                    }
                    break;
                // Case 24 CONTAINSHELP
                case 24:
                    if (isIdentChar(nextChar)) {
                        mark();
                    } else {
                        mark();
                        state = 100;
                    }
                    break;
                // Case 25 - 28 comments
                case 25:
                    if (nextChar == '/') {
                        mark();
                        state = 26;
                    } else if (nextChar == '*') {
                        mark();
                        state = 27;
                    } else {
                        state = 100;
                    }
                    break;
                case 26:
                    if (nextChar == '\n') {
                        mark();
                        state = 0;
                        start = current;
                    }
                    if (nextChar == -1) {
                        t.kind = Token.Type.EOF;
                        mark();
                        state = 100;
                    }
                    break;
                case 27:
                    if (nextChar == '*') {
                        mark();
                        state = 28;
                    }
                    break;
                case 28:
                    if (nextChar == '/') {
                        mark();
                        state = 0;
                        start = current;
                    } else {
                        mark();
                        state = 27;
                    }
                    break;
                // Case 29 INC
                case 29:
                    if (nextChar == '+') {
                        mark();
                        t.kind = Token.Type.INC;
                        start = current;
                    }
                    state = 100;
                    break;      
                //REGEX
                case 50:
                    switch (nextChar) {
                        case '(':
                            mark();
                            t.kind = Token.Type.RE_BRACKETOPEN;
                            state = 100;
                            break;
                        case ')':
                            mark();
                            t.kind = Token.Type.RE_BRACKETCLOSE;
                            state = 100;
                            break;
                        case '|':
                            mark();
                            t.kind = Token.Type.RE_OR;
                            state = 100;
                            break;
                        case '[':
                            mark();
                            t.kind = Token.Type.RE_SBOPEN;
                            state = 100;
                            break;
                        case ']':
                            mark();
                            t.kind = Token.Type.RE_SBCLOSE;
                            state = 100;
                            break;
                        case '\'':
                            mark();
                            t.kind = Token.Type.RE_CHAR;
                            state = 51;
                            start++;
                            break;
                        case '-':
                            mark();
                            t.kind = Token.Type.RE_RANGE;
                            state = 100;
                            break;
                        case '*':
                            mark();
                            t.kind = Token.Type.RE_COVER;
                            state = 100;
                            break;
                        case '?':
                            mark();
                            t.kind = Token.Type.RE_OPT;
                            state = 100;
                            break;
                        case '+':
                            mark();
                            t.kind = Token.Type.RE_PLUS;
                            state = 100;
                            break;
                        case '"':
                            mark();
                            t.kind = Token.Type.RE_MARK;
                            state = 100;
                            break;
                        case ',':
                            mark();
                            t.kind = Token.Type.RE_COMMA;
                            state = 100;
                            break;
                        case '!':
                            mark();
                            t.kind = Token.Type.RE_NOT;
                            state = 100;
                            break;
                        case '/':
                            mark();
                            if(firstInRegex) {
                                t.kind = Token.Type.RE_SLASH;
                                firstInRegex = false;
                            }
                            else {
                                t.kind = Token.Type.RE_SLASH;
                                lastInRegex = true;
                            }
                            state = 100;
                            break;
                        case '>':
                            mark();
                            if(lastInRegex) {
                                t.kind = Token.Type.FAEND;
                                inRegex = false;
                                lastInRegex = false;
                            } else {
                                t.kind = Token.Type.ERROR;
                                lastInRegex = false;
                                logger.info("Error in Lexer at case 50 in Regex / case. / in wrong place.");
                            }
                            state = 100;
                            break;
                        case ' ':
                        case '\t':
                        case '\n':
                        case '\r':
                            mark();
                            start = current;
                            break;
                        default:
                            if (isChar(nextChar)) {
                                t.kind = Token.Type.RE_CHAR;
                            } else {
                                t.kind = Token.Type.ERROR;
                                logger.info("Error in Lexer at case 50 in Regex default case. Unknow Token.");
                            }
                            mark();
                            state = 100;
                    }
                    firstInRegex = false;
                    break;
                case 51:
                    mark();
                    state = 52;
                    break;
                case 52:
                    if (nextChar == '\'') {
                        startOffset++;
                        state = 100;
                    } else {
                        mark();
                        t.kind = Token.Type.ERROR;
                        logger.info("Error in Lexer at case 52 in Regex. No closing tik after char.");
                        start--;
                        state = 100;
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
        start = start + startOffset;
        current = start;
    }

    public String proceed() {
        String erg = "";
        for (; start <= marked; start++)
            erg += (char) (int) buffer.get(start);
        reset();
        logger.info("content: " + erg);
        return erg;
    }
}
