

public abstract class AbstractTransition {
   private State start, end;
   public AbstractTransition(State s, State e) {
       start = s;
       end = e;
   }
   public State getStart() { return start; }
   public State getEnd() { return end; }
}
