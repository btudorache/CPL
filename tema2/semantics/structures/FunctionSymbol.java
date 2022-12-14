package cool.semantics.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionSymbol extends IdSymbol implements Scope {
    protected Map<String, Symbol> symbols = new LinkedHashMap<>();

    protected ClassSymbol parent;

    public FunctionSymbol(ClassSymbol parent, String name) {
        super(name);
        this.parent = parent;
    }

    public FunctionSymbol(ClassSymbol parent, String name, ClassSymbol classSymbol) {
        super(name, classSymbol);
        this.parent = parent;
    }

    @Override
    public boolean add(Symbol sym) {
        if (symbols.containsKey(sym.getName()))
            return false;

        symbols.put(sym.getName(), sym);

        return true;
    }

    @Override
    public Symbol lookup(String s) {
        var sym = symbols.get(s);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup(s);

        return null;
    }

    @Override
    public ClassSymbol lookupClass() {
        return this.parent;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    public Map<String, Symbol> getFormals() {
        return symbols;
    }
}