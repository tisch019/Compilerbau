import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Extensions {
    static Set<Value> removeAll(Set<Value> setA, Set<Value> setB){
        Set<Value> res = new HashSet<Value>();

        Iterator<Value> it = setA.iterator();
        // HashSet wird mit dem Iterator durchlaufen
        while (it.hasNext())
        {
            // Next gibt das aktuelle HashSet-Objekt zurück 
            // und geht zum nächsten über
            Value setLeft = (Value) it.next();
            Iterator<Value> itright = setB.iterator();
            boolean isPresent = false;

            while(itright.hasNext())
            {
                
                Value rightside = (Value) itright.next();

                if (setLeft.toString().equals(rightside.toString())) {

                    isPresent = true;
                }
            }

            if(!isPresent)
            {
                res.add(setLeft);
            }
            
        }

        return res;
    }

    static Set<Value> intersection(Set<Value> setA, Set<Value> setB){
        Set<Value> res = new HashSet<Value>();
        Iterator<Value> it = setA.iterator();
        // HashSet wird mit dem Iterator durchlaufen
        while (it.hasNext())
        {
            // Next gibt das aktuelle HashSet-Objekt zurück 
            // und geht zum nächsten über
            Value setLeft = (Value) it.next();
            Iterator<Value> itright = setB.iterator();
            boolean isPresent = false;

            while(itright.hasNext())
            {
                
                Value rightside = (Value) itright.next();

                if (setLeft.toString().equals(rightside.toString())) {

                    isPresent = true;
                }
            }

            if(isPresent)
            {
                res.add(setLeft);
            }
            
        }

        return res;
    }
}