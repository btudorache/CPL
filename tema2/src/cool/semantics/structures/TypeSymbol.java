package cool.semantics.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TypeSymbol extends Symbol {
    public static final TypeSymbol OBJECT   = new TypeSymbol("Object");
    public static final TypeSymbol INT   = new TypeSymbol("Int").setParentType(OBJECT);
    public static final TypeSymbol STRING   = new TypeSymbol("String").setParentType(OBJECT);
    public static final TypeSymbol BOOL  = new TypeSymbol("Bool").setParentType(OBJECT);
    public static final TypeSymbol IO  = new TypeSymbol("IO").setParentType(OBJECT);

    public static final TypeSymbol SELF_TYPE  = new TypeSymbol("SELF_TYPE");
    public static final List<String> illegatInheritableTypes = List.of(INT.getName(), STRING.getName(), BOOL.getName(), SELF_TYPE.getName());

    public static TypeSymbol LCA(TypeSymbol type1, TypeSymbol type2) {
        List<TypeSymbol> firstTypes = new ArrayList<>();
        TypeSymbol runner1 = type1;
        do {
            firstTypes.add(runner1);
            runner1 = runner1.parentType;
        } while (runner1 != null);
        Collections.reverse(firstTypes);

        List<TypeSymbol> secondTypes = new ArrayList<>();
        TypeSymbol runner2 = type2;
        do {
            secondTypes.add(runner2);
            runner2 = runner2.parentType;
        } while (runner2 != null);
        Collections.reverse(secondTypes);

        int smallerLength = Math.min(firstTypes.size(), secondTypes.size());
        TypeSymbol lca = firstTypes.get(0);
        for (int i = 0; i < smallerLength; i++) {
            var firstTypeName = firstTypes.get(i).getName();
            var secondTypeName = secondTypes.get(i).getName();
            if (!firstTypeName.equals(secondTypeName)) {
                break;
            }
            lca = firstTypes.get(i);
        }

        return lca;
    }
    private TypeSymbol parentType = null;
    public TypeSymbol(String name) {
        super(name);
    }

    public TypeSymbol setParentType(TypeSymbol parentType) {
        this.parentType = parentType;
        return this;
    }

    public boolean compareType(TypeSymbol challenger) {
        if (name.equals(challenger.getName())) {
            return true;
        }

        var parentTypeRunner = challenger.parentType;
        while (parentTypeRunner != null) {
            if (name.equals(parentTypeRunner.getName())) {
                return true;
            }
            parentTypeRunner = parentTypeRunner.parentType;
        }

        return false;
    }
}
