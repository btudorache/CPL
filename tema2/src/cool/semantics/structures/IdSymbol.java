package cool.semantics.structures;

import java.lang.reflect.Type;

public class IdSymbol extends Symbol {
    protected TypeSymbol type;

    public IdSymbol(String name) {
        super(name);
    }

    public IdSymbol(String name, TypeSymbol type) {
        super(name);
        this.type = type;
    }

    public void setType(TypeSymbol type) {
        this.type = type;
    }

    public TypeSymbol getType() {
        return type;
    }
}
