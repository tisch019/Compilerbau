import java.util.HashSet;
import java.util.Set;

public class FiniteAutomata {
    private State start;
    private HashSet<State> states = new HashSet<>();
    private Set<AbstractTransition> transitions = new HashSet<>();

    public FiniteAutomata(State start) {
        this.start = start;
        states.add(start);
    }
    public State getStartState() { return start; }
    public FiniteAutomata addTransitions(AbstractTransition t) {
        transitions.add(t);
        states.add(t.getEnd());
        states.add(t.getStart());
        return this;
    }
    public HashSet<State> getStates() { return states; }

    public static FiniteAutomata union(State start, FiniteAutomata A, FiniteAutomata B) {
        FiniteAutomata erg = new FiniteAutomata(start);
        erg.addTransitions(new EpsilonTransition(start, A.start));
        erg.addTransitions(new EpsilonTransition(start, B.start));
        return erg;
    }

    public String toString() {
        String erg =  "(" + start + ", { ";
        boolean first = false;
        for (State s:states)
            if (first) { erg += s; first=false; }
            else erg += ", "+s;
        erg += "}, T) with T:\n";
        for (AbstractTransition tr:transitions) {
            erg += "  " + tr + "\n";
        }
        return erg + "\n";
    }
}
