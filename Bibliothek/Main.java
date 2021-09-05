
// Diese Klasse ist kein Teil der Bibliothek
// Sie soll Beispiele für die Anwendung der Bibliothek geben

public class Main {
    public static void main(String[] args) {
        // Ranges:
        // einen Bereich ['a'-'z'] anlegen:
        Range range = new Range('a','z');
        // Zeichen '?' dem Bereich hinzufügen:
        range.add('?');
        // einen ganzen Bereich ['A'-'Z'] von Zeichen hinzufügen:
        range.add('A','Z');
        // Bereich ausgeben:
        System.out.println(range);

        // Endliche Automaten:
        FiniteAutomata fa2 = new FiniteAutomata(new State("startState"));
        // Hinzufügen eines Epsilon-Übergangs
        fa2.addTransitions(new EpsilonTransition(fa2.getStartState(), new State("Zweiter", 1)));
        // Hinzufügen einer "normalen" Kante
        fa2.addTransitions(new Transition(fa2.getStartState(), new State("Dritter", 1),new Range('a','z')));
        // und auch toString ist überschrieben ...
        System.out.println(fa2);

        // Reguläre Ausdrücke:
        // Beispiel ( "abc" | ['A'-'Z'] )*
        RangeExpr a = new RangeExpr(new Range('a')); // 'a'
        RangeExpr b = new RangeExpr(new Range('b')); // 'b'
        RangeExpr c = new RangeExpr(new Range('c')); // 'c'
        RangeExpr d = new RangeExpr(new Range('A','Z')); // ['A'-'Z']
        Concat c1 = new Concat(a,b); // ab
        Concat c2 = new Concat(c1,c); // abc
        Or or = new Or(c2,d); // ("abc" | ['A'-'Z'])
        Star ra = new Star(or); // ("abc" | ['A'-'Z'])*

        // Einen endlichen Automaten hieraus erzeugen:
        FiniteAutomata fa = ra.berrySethi(1);
        System.out.println(fa);
    }
}
