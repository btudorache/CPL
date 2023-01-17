package cool.semantics.structures;

public class IdSymbol extends Symbol {
    public ClassSymbol type;
    public ClassSymbol dynamicType;
    public String typeString;

    public IdSymbolType idType;
    public int offset;
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
