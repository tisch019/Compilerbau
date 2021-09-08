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
        RANGESTART,     // [
        RANGEEND,       // ]
        STATE,          // $"..."
        STATEACC,       // $"..."^1
        TRANSSTART,     // --
        TRANSEND,       // -->
        TRANSEPSI,      // --->
        FASTART,        // <
        FAEND,          // >
        RA,             // /.../
        SET,            // {...} von BLOCKSTART zu unterscheiden
        MAPSTART,       // [["
        MAPEND,         // ]]
        ARRAYSTART,     // [[
        ARRAYEND,       // ]]
        PRINT,          // Print Befehl zur Ausgabe
        EOF             // End des Streams / Quelldatei
    };
    
    public Type kind;
    public String content;
    int index;
    int line, column;
}