public class Type {
     String name;
    public Type(String name) {
        this.name = name;
    }

    public static Type charType = new Type("char");
    public static Type stringType = new Type("String");
    public static Type intType = new Type("int");
    public static Type booleanType = new Type("boolean");
    public static Type arrayType = new Type("array");
    public static Type errorType = new Type("error");
    public static Type stateType = new Type("State");
    public static Type rangeType = new Type("Range");
    public static Type transitionType = new Type("Transition");
    public static Type epsilonTransitionType = new Type("epsilonTransition");
    public static Type finiteAutomataType = new Type("FA");
    public static Type regularExpressionType = new Type("RE");
    public static Type setType = new Type("Set");
    public static Type mapType = new Type("Map");


    public String toString() {
        return name;
    }

    public static Type getType(String name) {
        switch(name)
        {
            case("char"):
                return charType;
            case("string"):
                return stringType;
            case("int"):
                return intType;
            case("boolean"):
                return booleanType;
            case("State"):
                return stateType;
            case("Range"):
                return rangeType;
            case("Transition"):
                return transitionType;
            case("epsilonTransition"):
                return epsilonTransitionType;
            case("FA"):
                return finiteAutomataType;
            case("RE"):
                return regularExpressionType;
            case("Set"):
                return setType;
            case("Map"):
                return mapType;
            default:
                return errorType;
        }
    }

    public static boolean canCast(Type t1, Type t2) {
        if (t1==errorType || t2==errorType) return true;
        if (t1==t2) return true;
        else if (t1 == finiteAutomataType && t2 == regularExpressionType) return true;
        else return false;
    }
    public static Type kgT(Type t1, Type t2) {
        if (t1==errorType || t2==errorType) return errorType;
        if (t1==t2) return t1;
        else if (t1==finiteAutomataType && t2==regularExpressionType) return finiteAutomataType; //fa + ra = fa
        else if (t1==regularExpressionType && t2==finiteAutomataType) return finiteAutomataType; //ra + fa = fa
        else if (t1==finiteAutomataType && t2==transitionType) return finiteAutomataType; // fa + transition = fa
        else return errorType;
    }
}
