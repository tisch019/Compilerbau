public class Type {
    String name;
    public Type(String name) {
        this.name = name;
    }

    public static Type doubleType = new Type("double");
    public static Type intType = new Type("int");
    public static Type booleanType = new Type("boolean");
    public static Type errorType = new Type("error");
    public static Type stateType = new Type("state");
    public static Type rangeType = new Type("range");
    public static Type transitionTape = new Type("transition");
    public static Type finiteAutomataType = new Type("finiteAutomata");
    public static Type regularExpressionType = new Type("regularExpression");

    public String toString() {
        return name;
    }

    public static Type getType(String name) {
        switch(name)
        {
            case("double"):
                return doubleType;
            case("int"):
                return intType;
            case("boolean"):
                return booleanType;
            case("state"):
                return stateType;
            case("range"):
                return rangeType;
            case("transition"):
                return transitionTape;
            case("finiteAutomata"):
                return finiteAutomataType;
            case("regularExpression"):
                return regularExpressionType;
            default:
                return errorType;
        }
    }

    public static boolean canCast(Type t1, Type t2) {
        if (t1==errorType || t2==errorType) return true;
        if (t1==t2) return true;
        else if (t1==intType && t2==doubleType) return true;
        else if (t1 == finiteAutomataType && t2 == regularExpressionType) return true;
        else return false;
    }
    public static Type kgT(Type t1, Type t2) {
        if (t1==errorType || t2==errorType) return errorType;
        if (t1==t2) return t1;
        else if (t1==doubleType && t2==intType) return doubleType;
        else if (t1==intType && t2==doubleType) return doubleType;
        else if (t1==finiteAutomataType && t2==regularExpressionType) return finiteAutomataType; //fa + ra = fa
        else if (t1==regularExpressionType && t2==finiteAutomataType) return finiteAutomataType; //ra + fa = fa
        else if (t1==finiteAutomataType && t2==transitionTape) return finiteAutomataType; // fa + transition = fa
        else return errorType;
    }
}