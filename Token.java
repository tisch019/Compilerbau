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
        SQUAREBRACKETOPEN,  //[
        SQUAREBRACKETCLOSE, //]
        SEM,            // ;
        COMMA,          // ,
        SETTO,          // Zuweisung, einzelnes =
        COMP,           // Vergleich ==, !=, >= , <=
        IDENTIFIER,     // Folge an Characters
        IFSTART,        // if(
        ELSE,           // else
        WHILE,          // while
        PRINT,          // Print Befehl zur Ausgabe
                    //Schlüsselworte
        KEYSTRING,      // String Schlüsselwort
        KEYCHAR,        // Char Schlüsselwort
        KEYINT,         // Int Schlüsselwort
        KEYBOOL,        // Bool Schlüsselwort
        KEYRANGE,       // Schlüsselwort Range
        KEYSTATE,       // Schlüsselwort State
        KEYTRANSITION,  // Schlüsselwort Transition
        KEYFA,          // Schlüsselwort FA
        KEYRA,          // Schlüsselwort RA
        KEYSET,         // Schlüsselwort Set
        KEYMAP,         // Schlüsselwort Map
                    //Values
        STRING,         // String Value
        CHAR,           // Char Value 'a'
        INT,            // Int Value
        BOOL,           // Bool Value
        STATE,          // $"..."
        STATEACC,       // $"..."^1
        TRANSSTART,     // --
        TRANSEND,       // -->
        TRANSEPSI,      // --->
        FASTART,        // <
        FAEND,          // >
        RA,             // /.../
        SET,            // {...} von BLOCKSTART zu unterscheiden
        TYPE,           // Bsp. Map<type1,type2> braucht Type Token
        MAPDELI,        // :
        MAPARRAYSTART,  // [[
        MAPARRAYEND,    // ]]
        ERROR,          // Lexer Error
        WS,             // whitespace ' ', \t, \n, \r
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