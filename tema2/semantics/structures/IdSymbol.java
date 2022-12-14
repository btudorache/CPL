package cool.semantics.structures;

import java.lang.reflect.Type;

public class IdSymbol extends Symbol {
    public ClassSymbol type;

    public IdSymbol(String name) {
        super(name);
    }

    public IdSymbol(String name, ClassSymbol type) {
        super(name);
        this.type = type;
    }
}
