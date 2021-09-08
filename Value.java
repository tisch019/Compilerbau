public class Value {
    Type type;
    boolean b;
    int i;
    double d;

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
    public Value(Value c) { type = c.type; d =c.d; i=c.i; b=c.b; }
    public Value(Type t, Object v) {
        if (t == Type.doubleType) {
            d = (Double) v;
        }
    }
    public Value copy() {
        return new Value(this);
    }
}
