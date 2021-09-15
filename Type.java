import java.util.*;

public class Type {
    String name;
    List<Type> genericTypes = new ArrayList<Type>();

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

   //RE-Types aus der Klasse RegularExpression.java
   public static Type orType = new Type("Or");
   public static Type concatType = new Type("Concat");
   public static Type starType = new Type("Star");
   public static Type rangeExprType = new Type("RangeExpr");
   public static Type emptyWordType = new Type("EmptyWord");


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
           case("Or"):
               return orType;
           case("Concat"):
               return concatType;
           case("Star"):
               return starType;
           case("RangeExpr"):
               return rangeExprType;
           case("EmptyWord"):
               return emptyWordType;
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

    public void addGenTyp(Type t){
        genericTypes.add(t);
    }

    public Type copy(){
        return new Type(this.name);
    }
    
    @Override
    public boolean equals(Object obj){
        Type t = (Type) obj;
        boolean ret = false;
        if(t.name == this.name)
            ret = true;
        if(t.genericTypes.size() == this.genericTypes.size()){
            for(int i = 0; i<t.genericTypes.size();i++){
                if(t.genericTypes.get(i) == this.genericTypes.get(i))
                    ret = true;
                else
                    return false;
            }
        }else if(t.genericTypes.size()==0)
            ret = true;
        else
            return false;
        return ret;
    }
}
