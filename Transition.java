

public class Transition extends AbstractTransition {
    private Range range;

    public Transition(State start, State end, Range r) {
        super(start,end);
        range = r;
    }
    public Range getRange() { return range; }
    public String toString() { return ""+getStart()+" -- " + range + " --> "+getEnd();  }
}
