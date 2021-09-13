import java.util.HashMap;
import java.util.Map;

import TestComp.DeclNode;

class TypeNodePair {
    Type type;
    DeclNode node;
    public TypeNodePair(Type type, DeclNode node) {
        this.type = type; this.node = node;
    }
}

public class SymbolTabelle {
    Map<String, TypeNodePair> table = new HashMap<>();
    SymbolTabelle parent;

    public SymbolTabelle(SymbolTabelle p) {
        parent = p;
    }
    public TypeNodePair find(String x) {
        if (table.containsKey(x)) return table.get(x);
        else if (parent!=null) return parent.find(x);
        else return null;
    }
    public void add(String x, Type t, DeclNode n) {
        TypeNodePair ai = find(x);
        if (ai==null) table.put(x, new TypeNodePair(t,n));
        else table.put(x, new TypeNodePair(Type.errorType, n));
    }
}
