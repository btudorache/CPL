package cool.semantics.structures;

import cool.ast.nodes.Id;
import org.antlr.v4.runtime.Token;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassSymbol extends TypeSymbol implements Scope {
    protected Map<String, IdSymbol> variableSymbols = new LinkedHashMap<>();
    protected Map<String, FunctionSymbol> functionSymbols = new LinkedHashMap<>();

    protected Scope parent;

    protected ClassSymbol inheritedScope;

    public ClassSymbol(Scope parent, String name) {
        super(name);
        this.parent = parent;
    }

    public void setInheritedScope(ClassSymbol inheritedScope) {
        this.inheritedScope = inheritedScope;
        setParentType(inheritedScope);
    }

    public ClassSymbol getInheritedScope() {
        return this.inheritedScope;
    }

    @Override
    public boolean add(Symbol sym) {
        if (sym instanceof FunctionSymbol) {
            if (functionSymbols.containsKey(sym.getName())) {
                return false;
            }

            functionSymbols.put(sym.name, (FunctionSymbol) sym);
            return true;
        } else if (sym instanceof IdSymbol) {
            if (variableSymbols.containsKey(sym.getName())) {
                return false;
            }

            variableSymbols.put(sym.name, (IdSymbol) sym);
            return true;
        }

        return false;
    }

    @Override
    public Symbol lookup(String s) {
        return lookupMember(s);
    }

    @Override
    public ClassSymbol lookupClass() {
        return this;
    }

    public Symbol lookupInheritanceTree(String s) {
        if (inheritedScope != null) {
            return inheritedScope.recLookupInheritanceTree(s);
        }

        return null;
    }

    private Symbol recLookupInheritanceTree(String s) {
        var foundSymbol = lookupMember(s);
        if (foundSymbol != null) {
            return foundSymbol;
        }

        if (inheritedScope != null) {
            return inheritedScope.recLookupInheritanceTree(s);
        }

        return null;
    }

    private Symbol lookupMember(String s) {
        var functionSymbol = functionSymbols.get(s);
        if (functionSymbol != null) {
            return functionSymbol;
        }

        var variableSymbol = variableSymbols.get(s);
        if (variableSymbol != null) {
            return variableSymbol;
        }

        if (parent != null) {
            return parent.lookup(s);
        }

        return null;
    }

    @Override
    public Scope getParent() {
        return parent;
    }
}