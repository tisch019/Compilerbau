package Bibliothek;

public class EpsilonTransition extends AbstractTransition {
    public EpsilonTransition(State start, State end) {
        super(start,end);
    }
    public String toString() { return ""+getStart()+" --> "+getEnd();  }
}
