
import java.util.Map;
import java.util.Set;


import Bibliothek.*;

public class Value {
    Type type;
    boolean b;
    int i;
    double d;
    State s;
    Range r;
    Transition t;
    EpsilonTransition et;
    FiniteAutomata fa;
    RegularExpression re;
    Set<Value> st;
    Map<Value, Value> mp;

    public Value() {};

    public String toString() {
        String erg = ""+type+" ";
        if (type==Type.booleanType) erg+= b;
        else if (type==Type.doubleType) erg+= d;
        else erg += i;
        return erg;
    }

    public Value(boolean b) { type = Type.booleanType; this.b=b; };
    public Value(int i) { type = Type.intType; this.i=i; };
    public Value(double d) { type = Type.doubleType; this.d=d; };
    public Value(State s) { type = Type.stateType; this.s = s;}
    public Value(Range r) { type = Type.rangeType; this.r = r;}
    public Value(Transition t) { type = Type.transitionType; this.t = t;}
    public Value(EpsilonTransition et) { type = Type.epsilonTransitionType; this.et = et;}
    public Value(FiniteAutomata fa) { type = Type.finiteAutomataType; this.fa = fa;}
    public Value(RegularExpression re) { type = Type.regularExpressionType; this.re = re;}
    public Value(Set<Value> st) { type = Type.setType; this.st = st;}
    public Value(Map<Value,Value> mp) { type = Type.setType; this.mp = mp;}
    public Value(Value c) { type = c.type; d =c.d; i=c.i; b=c.b; s=c.s; r=c.r; t=c.t; et=c.et; fa=c.fa; re=c.re; st=c.st; mp=c.mp;}

    public Value(Type x, Object v) {
        if (x == Type.doubleType) d = (Double) v;
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
