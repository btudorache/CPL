package cool.semantics.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalScope implements Scope {
    private final Map<String, ClassSymbol> symbols = new LinkedHashMap<>();

    public GlobalScope() {}

    public ClassSymbol lookupType(String name) { return symbols.get(name); }

    @Override
    public boolean add(Symbol sym) {
        if (symbols.containsKey(sym.getName()))
            return false;

        if (sym instanceof ClassSymbol) {
            symbols.put(sym.getName(), (ClassSymbol) sym);
            return true;
        }

        return false;
    }

    @Override
    public Symbol lookup(String name) {
        return symbols.get(name);
    }

    @Override
    public ClassSymbol lookupClass() {
        return null;
    }

    @Override
    public Scope getParent() {
        return null;
    }

    @Override
    public String toString() {
        return symbols.values().toString();
    }
}
