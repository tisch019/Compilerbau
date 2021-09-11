import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import Bibliothek.*;

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

    public Value() {};

    public String toString() {
        //TODO
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
    public Value(Map<Value,Value> mp) { type = Type.setType; this.mp = mp;}
    public Value(List<Value> ar) { type = Type.arrayType; this.ar = ar;}
    public Value(Value va[]) { type = Type.arrayType; this.va = va;}
    public Value(Value c) { type = c.type; this.c =c.c; i=c.i; b=c.b; s=c.s; r=c.r; t=c.t; et=c.et; fa=c.fa; re=c.re;st=c.st; mp=c.mp;}

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
