public class Token
{
    public enum Type
    {
        LOP,            // % * /
        POP,            // + -
        BOOLOP,         // BoolOperationen: == , >= , <=
        BOOLNEG,        // !
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