import java.util.Set;

public class Token
{
    public enum Type
    {
        BINOP,          // Binäre Operation, + - * /
                        // an dieser Stelle macht Rettinger POP und LOP
                        // er baut seine Gramatik wie folgt auf
                        //          sum = { prod { ("+"|"-") prod } }
                        //          prod = { atom { ("*"|"/"|"%") atom } }
                        // ist das für Punkt for Strich?
        LOP,            // % * /
        POP,            // + -
        BOOLOP,         // !, ggf weitere wie == , >= , <=
        BLOCKSTART,     // {
        BLOCKEND,       // }
        BRACKETSTART,   // (
        BRACKETEND,     // )
        SEM,            // ;
        SETTO,          // Zuweisung, einzelnes =
        COMP,           // Vergleich ==
        IDENTIFIER,     // Folge an Characters
        KEYDOUBLE,      // Double Schlüsselwort
        DOUBLE,         // Double Value
        KEYINT,         // Int Schlüsselwort
        INT,            // Int Value
        KEYBOOL,        // Bool Schlüsselwort
        BOOL,           // Bool Value
        IFSTART,        // if(
        ELSE,           // else
        WHILE,          // while
        ERROR,          // Lexer Error
        WS,             // whitespace ' ', \t, \n, \r
        RANGE,          // [...]
        STATE,          // $"..."
        TRANSSTART,     // --
        TRANSEND,       // -->
        TRANSEPSI,      // --->
        FASTART,        // <
        FAEND,          // >
        RA,             // /.../
        SET,            // {...} von BLOCKSTART zu unterscheiden
        MAP,            // [["...":1,"...":2,...]]
        ARRAY,          // [[...]]
        EOF             // End des Streams / Quelldatei
    };
    
    public Type kind;
    public String content;
    int index;
    int line, column;
}