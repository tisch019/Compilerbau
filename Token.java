public class Token
{
    public enum Type
    {
        LOP,            // % * /
        POP,            // + -
        BOOLNEG,        // !
        BLOCKSTART,     // {
        BLOCKEND,       // }
        BRACKETSTART,   // (
        BRACKETEND,     // )
        SEM,            // ;
        COMMA,          // ,
        SETTO,          // Zuweisung, einzelnes =
        COMP,           // Vergleich ==, !=, >= , <=
        IDENTIFIER,     // Folge an Characters
        KEYSTRING,      // String Schlüsselwort
        STRING,         // String Value
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
        KEYRANGE,       // Schlüsselwort Range
        RANGESTART,     // [
        RANGESINGLE,    // character single (z.B. a)
        RANGEBRSTART,   // Character mit 'a'
        RANGEBREND,     // character z (Bsp.: a - z)
        RANGEEND,       // ]
        KEYSTATE,       // Schlüsselwort State
        STATE,          // $"..."
        STATEACC,       // $"..."^1
        KEYTRANSITION,  // Schlüsselwort Transition
        TRANSSTART,     // --
        TRANSEND,       // -->
        TRANSEPSI,      // --->
        KEYFA,          // Schlüsselwort FA
        FASTART,        // <
        FAEND,          // >
        KEYRA,          // Schlüsselwort RA
        RA,             // /.../
        KEYSET,         // Schlüsselwort Set
        SET,            // {...} von BLOCKSTART zu unterscheiden
        KEYMAP,         // Schlüsselwort Map
        TYPE,           // Bsp. Map<type1,type2> braucht Type Token
        MAPSTART,       // [["
        MAPEND,         // ]]
        ARRAYSTART,     // [[
        ARRAYEND,       // ]]
        PRINT,          // Print Befehl zur Ausgabe
        EOF,            // End des Streams / Quelldatei

        //Token für Reguläre Ausdrücke
        RE_BRACKETOPEN, // (
        RE_BRACKETCLOSE,// )
        RE_MARK,        // "
        RE_CHAR,        // character single
        RE_OR,          // |
        RE_SBOPEN,      // [
        RE_SBCLOSE,     // ]
        RE_SINGLEMARK,  // '
        RE_RANGECHAR,   // A
        RE_RANGE,       // -
        RE_COVER        // *
    };
    
    public Type kind;
    public String content;
    int index;
    int line, column;
}