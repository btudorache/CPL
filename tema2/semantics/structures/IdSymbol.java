package cool.semantics.structures;

import java.lang.reflect.Type;

public class IdSymbol extends Symbol {
    public ClassSymbol type;
    public ClassSymbol dynamicType;
    public String typeString;

    public IdSymbol(String name) {
        super(name);
    }

    public IdSymbol(String name, String typeString) {
        super(name);
        this.typeString = typeString;
    }

    public IdSymbol(String name, ClassSymbol type) {
        super(name);
        this.type = type;
    }
}
