import Bibliothek.*;

public class Value {
    Type type;
    boolean b;
    int i;
    char c;
    State s;
    Range r;
    Transition t;
    EpsilonTransition et;
    FiniteAutomata fa;
    RegularExpression re;

    public Value() {};

    public String toString() {
        //TODO
    }

    public Value(boolean b) { type = Type.booleanType; this.b=b; };
    public Value(int i) { type = Type.intType; this.i=i; };
    public Value(char c){ type = Type.charType; this.c=c;};
    public Value(State s) { type = Type.stateType; this.s = s;}
    public Value(Range r) { type = Type.rangeType; this.r = r;}
    public Value(Transition t) { type = Type.transitionType; this.t = t;}
    public Value(EpsilonTransition et) { type = Type.epsilonTransitionType; this.et = et;}
    public Value(FiniteAutomata fa) { type = Type.finiteAutomataType; this.fa = fa;}
    public Value(RegularExpression re) { type = Type.regularExpressionType; this.re = re;}
    public Value(Value c) { type = c.type; this.c =c.c; i=c.i; b=c.b; s=c.s; r=c.r; t=c.t; et=c.et; fa=c.fa; re=c.re;}

    public Value copy() {
        return new Value(this);
    }
}
