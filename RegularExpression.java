

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

class Or extends RegularExpression {
    RegularExpression left;
    RegularExpression right;

    public Or(RegularExpression l, RegularExpression r) {
        left = l; right = r;
    }
    @Override boolean computeEmpty() { return isEmpty = left.computeEmpty() | right.computeEmpty(); }
    @Override HashSet<Leaf> computeFirst() {
        first.addAll(left.computeFirst());
        first.addAll(right.computeFirst());
        return first;
    }
    @Override HashSet<Leaf> computeLast() {
        last.addAll(left.computeLast());
        last.addAll(right.computeLast());
        return last;
    }
    @Override void computeNext(HashSet<Leaf> n) {
        next = n;
        left.computeNext(next);
        right.computeNext(next);
    }
}

class Concat extends RegularExpression {
    RegularExpression left;
    RegularExpression right;

    public Concat(RegularExpression l, RegularExpression r) {
        left = l; right = r;
    }

    @Override boolean computeEmpty() { return isEmpty = left.computeEmpty() & right.computeEmpty(); }
    @Override HashSet<Leaf> computeFirst() {
        if (!left.isEmpty) {
            first = left.computeFirst();
            right.computeFirst();
        }
        else {
            first.addAll(left.computeFirst());
            first.addAll(right.computeFirst());
        }
        return first;
    }
    @Override HashSet<Leaf> computeLast() {
        if (!right.isEmpty) {
            left.computeLast();
            last = right.computeLast();
        }
        else {
            last.addAll(left.computeLast());
            last.addAll(right.computeLast());
        }
        return last;
    }
    @Override void computeNext(HashSet<Leaf> n) {
        right.computeNext(n);
        next = n;
        if (!right.isEmpty) left.computeNext(right.first);
        else {
            HashSet<Leaf> temp = new HashSet<>();
            temp.addAll(n);
            temp.addAll(right.first);
            left.computeNext(temp);
        }
    }
}

class Star extends RegularExpression {
    RegularExpression content;

    public Star(RegularExpression c) {
        content = c;
    }

    @Override boolean computeEmpty() {
        content.computeEmpty();
        return isEmpty=true;
    }
    @Override HashSet<Leaf> computeFirst() { return first = content.computeFirst(); }
    @Override HashSet<Leaf> computeLast() { return last = content.computeLast(); }
    @Override void computeNext(HashSet<Leaf> n) {
       next.addAll(n);
       next.addAll(content.first);
       content.computeNext(next);
    }
}

class RangeExpr extends Leaf {
    Range range;
    public RangeExpr(Range r) {
       range = r;
    }
    @Override boolean computeEmpty() { return isEmpty=false; }
}

abstract class Leaf extends RegularExpression {
    @Override HashSet<Leaf> computeFirst() { first.add(this); return first; }
    @Override HashSet<Leaf> computeLast() { last.add(this); return last; }
    @Override void computeNext(HashSet<Leaf> n) { next = n; }
}

class EmptyWord extends Leaf {
    @Override boolean computeEmpty() { return isEmpty = true; }
}

public abstract class RegularExpression {
    boolean isEmpty;
    HashSet<Leaf> first = new HashSet<>();
    HashSet<Leaf> last = new HashSet<>();
    HashSet<Leaf> next = new HashSet<>();

    abstract boolean computeEmpty();
    abstract HashSet<Leaf> computeFirst();
    abstract HashSet<Leaf> computeLast();
    abstract void computeNext(HashSet<Leaf> n);

    FiniteAutomata berrySethi(int accept) {
        computeEmpty();
        computeFirst();
        computeLast();
        computeNext(new HashSet<>());


        int stateCounter = 1;
        State start;
        if (isEmpty) start = new State("S"+stateCounter++, accept);
        else start = new State("S"+stateCounter++);

        FiniteAutomata fa = new FiniteAutomata(start);

        HashMap<Leaf,State> map = new HashMap<>();
        HashSet<Leaf> markedStates = new HashSet<>();
        LinkedList<Leaf> stack = new LinkedList<>();
        // initialize stack
        stack.addAll(first);
        for(Leaf n:first) {
            if (n.isEmpty || !((RangeExpr)n).range.isEmpty()) {
                State nState = new State("S" + stateCounter++);
                if (last.contains(n)) nState.accept = accept;
                if (n.isEmpty) fa.addTransitions(new EpsilonTransition(start,nState));
                else if (!((RangeExpr)n).range.isEmpty()) fa.addTransitions(new Transition(start,nState,((RangeExpr)n).range));
                map.put(n,nState);
                stack.push(n);
            }
        }
        while (!stack.isEmpty()) {
            Leaf curLeaf = stack.pop();
            if (!markedStates.contains(curLeaf)) {
                markedStates.add(curLeaf);
                for(Leaf n:curLeaf.next) {
                    if (n.isEmpty || !((RangeExpr)n).range.isEmpty()) {
                        State nState;
                        if (map.containsKey(n)) nState = map.get(n);
                        else {
                            nState = new State("S" + stateCounter++);
                            map.put(n, nState);
                        }
                        if (last.contains(n)) nState.accept = accept;
                        if (n.isEmpty) fa.addTransitions(new EpsilonTransition(map.get(curLeaf),nState));
                        else if (!((RangeExpr)n).range.isEmpty()) fa.addTransitions(new Transition(map.get(curLeaf),nState,((RangeExpr)n).range));
                        if (!markedStates.contains(n)) stack.push(n);
                    }
                }
            }
        }
        return fa;
    }
}
