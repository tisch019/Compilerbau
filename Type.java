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

    public String toString() {
        return name;
    }

    public static Type getType(String name) {
        if (name=="double") return doubleType;
        else return intType;
    }

    public static boolean canCast(Type t1, Type t2) {
        if (t1==errorType || t2==errorType) return true;
        if (t1==t2) return true;
        else if (t1==intType && t2==doubleType) return true;
        else return false;
    }
    public static Type kgT(Type t1, Type t2) {
        if (t1==errorType || t2==errorType) return errorType;
        if (t1==t2) return t1;
        else if (t1==doubleType || t2==doubleType) return doubleType;
        else return intType;
    }
}
