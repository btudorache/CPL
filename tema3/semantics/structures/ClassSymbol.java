package cool.semantics.structures;

import java.util.*;
import java.util.stream.Collectors;

public class ClassSymbol extends Symbol implements Scope {
    public static ClassSymbol OBJECT;
    public static ClassSymbol IO;
    public static ClassSymbol INT;
    public static ClassSymbol STRING;
    public static ClassSymbol BOOL;
    public static ClassSymbol SELF_TYPE = new ClassSymbol(null, "SELF_TYPE");
    public static List<String> illegalInheritableTypes;
    
    static {
        OBJECT = new ClassSymbol(null, "Object");
        OBJECT.add(new FunctionSymbol(OBJECT, "abort", OBJECT));
        var objectTypeNameFunction = new FunctionSymbol(OBJECT, "type_name");
        OBJECT.add(objectTypeNameFunction);
        OBJECT.add(new FunctionSymbol(OBJECT, "copy", SELF_TYPE));


        INT = new ClassSymbol(null, "Int");
        INT.setInheritedScope(OBJECT);


        BOOL = new ClassSymbol(null, "Bool");
        BOOL.setInheritedScope(OBJECT);


        STRING = new ClassSymbol(null, "String");
        // the following line has to be set there
        objectTypeNameFunction.type = STRING;
        STRING.setInheritedScope(OBJECT);

        STRING.add(new FunctionSymbol(STRING, "length", INT, 12));

        FunctionSymbol stringConcat = new FunctionSymbol(STRING, "concat", STRING, 16);
        stringConcat.add(new IdSymbol("s", STRING));
        STRING.add(stringConcat);

        FunctionSymbol stringSubstr = new FunctionSymbol(STRING, "substr", STRING, 20);
        stringSubstr.add(new IdSymbol("i", INT));
        stringSubstr.add(new IdSymbol("l", INT));
        STRING.add(stringSubstr);


        IO = new ClassSymbol(null, "IO");
        IO.setInheritedScope(OBJECT);

        FunctionSymbol ioOutString = new FunctionSymbol(IO, "out_string", SELF_TYPE);
        ioOutString.add(new IdSymbol("x", STRING));
        IO.add(ioOutString);

        FunctionSymbol ioOutInt = new FunctionSymbol(IO, "out_int", SELF_TYPE);
        ioOutInt.add(new IdSymbol("x", INT));
        IO.add(ioOutInt);

        IO.add(new FunctionSymbol(IO, "in_string", STRING));
        IO.add(new FunctionSymbol(IO, "in_int", INT));



        illegalInheritableTypes = List.of(INT.getName(), STRING.getName(), BOOL.getName(), SELF_TYPE.getName());
    }

    public static ClassSymbol LCA(ClassSymbol type1, ClassSymbol type2) {
        List<ClassSymbol> firstTypes = new ArrayList<>();
        ClassSymbol runner1 = type1;
        do {
            firstTypes.add(runner1);
            runner1 = runner1.inheritedScope;
        } while (runner1 != null);
        Collections.reverse(firstTypes);

        List<ClassSymbol> secondTypes = new ArrayList<>();
        ClassSymbol runner2 = type2;
        do {
            secondTypes.add(runner2);
            runner2 = runner2.inheritedScope;
        } while (runner2 != null);
        Collections.reverse(secondTypes);

        int smallerLength = Math.min(firstTypes.size(), secondTypes.size());
        ClassSymbol lca = firstTypes.get(0);
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
    public Map<String, IdSymbol> variableSymbols = new LinkedHashMap<>();
    public Map<String, FunctionSymbol> functionSymbols = new LinkedHashMap<>();

    public Scope parent;

    public ClassSymbol inheritedScope;
    public int label;

    public int lowerClassBound;

    public ClassSymbol(Scope parent, String name) {
        super(name);
        this.parent = parent;
    }

    /**
     * Checks if challenger is a subtype of the current type
     * @param challenger
     * @return
     */
    public boolean compareType(ClassSymbol challenger) {
        if (name.equals(challenger.getName())) {
            return true;
        }

        var parentTypeRunner = challenger.inheritedScope;
        while (parentTypeRunner != null) {
            if (name.equals(parentTypeRunner.getName())) {
                return true;
            }
            parentTypeRunner = parentTypeRunner.inheritedScope;
        }

        return false;
    }

    public void setInheritedScope(ClassSymbol inheritedScope) {
        this.inheritedScope = inheritedScope;
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

    public List<String> getClassMethods() {
        var methodSymbols = gatherMethodNames();
        return methodSymbols.stream().map(methodSymbol -> String.format("%s.%s", methodSymbol.parent.name, methodSymbol.name)).collect(Collectors.toList());
    }
    public List<FunctionSymbol> gatherMethodNames() {
        if (inheritedScope == null) {
            return gatherMethodNamesRec(new LinkedList<>());
        }

        var parentMethods = inheritedScope.gatherMethodNames();
        return gatherMethodNamesRec(parentMethods);
    }

    private List<FunctionSymbol> gatherMethodNamesRec(List<FunctionSymbol> parentMethods) {
        var offset = parentMethods.size();
        for (var function : functionSymbols.values()) {
            var alreadyDefinedSuperMethod = parentMethods.stream().filter(method -> method.name.equals(function.name)).findFirst();

            if (alreadyDefinedSuperMethod.isPresent()) {
                var alreadyDefinedFuncSymbol = alreadyDefinedSuperMethod.get();
                function.dispatchTableOffset = alreadyDefinedFuncSymbol.dispatchTableOffset;
                for (int i = 0; i < parentMethods.size(); i++) {
                    if (parentMethods.get(i).name.equals(alreadyDefinedFuncSymbol.name)) {
                        parentMethods.set(i, function);
                        break;
                    }
                }
            } else {
                parentMethods.add(function);
                function.dispatchTableOffset = 4 * offset;
                offset++;
            }
        }

        return parentMethods;
    }

    public List<ClassSymbol> getClassVariables() {
        if (inheritedScope == null) {
            return gatherVariableTypes(new LinkedList<>());
        }

        var parentVariables = inheritedScope.getClassVariables();
        return gatherVariableTypes(parentVariables);
    }

    private List<ClassSymbol> gatherVariableTypes(List<ClassSymbol> parentVariables) {
        var offset = parentVariables.size();
        for (var key : variableSymbols.keySet()) {
            IdSymbol variableSymbol = variableSymbols.get(key);
            variableSymbol.idType = IdSymbolType.ClassIdSymbol;
            variableSymbol.offset = 12 + offset * 4;
            parentVariables.add(variableSymbol.type);
            offset++;
        }

        return parentVariables;
    }

    @Override
    public Scope getParent() {
        return parent;
    }
}