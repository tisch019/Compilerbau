import java.util.List;
import java.util.Map;
import java.util.Set;


public class Value {
    Type type;
    char c;
    String stg;
    boolean b;
    int i;
    State s;
    Range r;
    Transition t;
    EpsilonTransition et;
    FiniteAutomata fa;
    RegularExpression re;
    Set<Value> st;
    Map<Value, Value> mp;
    List<Value> ar;
    Value va[];
    

    //RE-Types aus der Klasse RegularExpression.java
    //Or or;
    //Concat con;
    //Star star;
    //RangeExpr rExpr;
    //EmptyWord eWord;

    public Value() {};

    public String toString() {
        if(type == Type.charType){
            return String.valueOf(c);
        }else if(type == Type.stringType){
            return stg;
        }else if(type == Type.intType){
            return String.valueOf(i);
        }else if(type == Type.booleanType){
            return String.valueOf(b);
        }else if(type == Type.arrayType){
            return va.toString();
        }else if(type == Type.stateType){
            return s.toString();
        }else if(type == Type.rangeType){
            return r.toString();
        }else if(type == Type.transitionType){
            return t.toString();
        }else if(type == Type.epsilonTransitionType){
            return et.toString();
        }else if(type == Type.finiteAutomataType){
            return fa.toString();
        }else if(type == Type.regularExpressionType){
            return re.toString();
        }else if(type.name == Type.setType.name){
            return st.toString();
        }else if(type.name == Type.mapType.name){
            return mp.toString();
        }else if(type == Type.arrayType){
            return ar.toString();
        }else return "Error Converting Value to String with Type "+type.toString();
    }

    public Value(String stg){ type = Type.stringType; this.stg=stg;};
    public Value(char c){ type = Type.charType; this.c=c;};
    public Value(boolean b) { type = Type.booleanType; this.b=b; };
    public Value(int i) { type = Type.intType; this.i=i; };
    public Value(State s) { type = Type.stateType; this.s = s;}
    public Value(Range r) { type = Type.rangeType; this.r = r;}
    public Value(Transition t) { type = Type.transitionType; this.t = t;}
    public Value(EpsilonTransition et) { type = Type.epsilonTransitionType; this.et = et;}
    public Value(FiniteAutomata fa) { type = Type.finiteAutomataType; this.fa = fa;}
    public Value(RegularExpression re) { type = Type.regularExpressionType; this.re = re;}
    public Value(Set<Value> st) { type = Type.setType; this.st = st;}
    public Value(Map<Value,Value> mp) { type = Type.mapType; this.mp = mp;}
    public Value(List<Value> ar) { type = Type.arrayType; this.ar = ar;}
    public Value(Value c) { type = c.type; this.c =c.c; stg=c.stg; b=c.b; i=c.i; s=c.s; r=c.r; t=c.t; et=c.et; fa=c.fa; re=c.re;st=c.st; mp=c.mp; ar=c.ar; va=c.va;}

    //RE-Values
    //public Value(Or or) {type = Type.orType; this.or = or;}
    //public Value(Concat con) {type = Type.concatType; this.con = con;}
    //public Value(Star star) {type = Type.starType; this.star = star;}
    //public Value(RangeExpr rExpr) {type = Type.rangeExprType; this.rExpr = rExpr;}
    //public Value(EmptyWord eWord) {type = Type.emptyWordType; this.eWord = eWord;}

    //public Value(Set<Node> st) { type = Type.setType; this.st = st;}
    //public Value(Map<Node,Node> mp) { type = Type.setType; this.mp = mp;}

    public Value(Type x, Object v) {
        if(x == Type.charType) c = (char) v;
        else if(x == Type.stringType) stg = (String) v;
        else if(x == Type.booleanType) b = (Boolean) v;
        else if(x == Type.intType) i = (Integer) v;
        else if(x == Type.stateType) s = (State) v;
        else if(x == Type.rangeType) r = (Range) v;
        else if(x == Type.transitionType) t = (Transition) v;
        else if(x == Type.epsilonTransitionType) et = (EpsilonTransition) v;
        else if(x == Type.finiteAutomataType) fa = (FiniteAutomata) v;
        else re = (RegularExpression) v;
    }

    public Value copy() {
        return new Value(this);
    }
}
