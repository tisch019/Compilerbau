public class State {
    String label;
    int accept;

    public State(String l) {
        label = l;
    }
    public State(String l, int a) {
        this(l);
        accept = a;
    }
    public String toString() {
        return "(" + label + ", "+accept+")";
    }
}
