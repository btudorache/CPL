package cool.semantics;

import cool.ast.ASTVisitor;
import cool.ast.nodes.*;
import cool.ast.nodes.Class;
import cool.semantics.structures.*;

import static cool.semantics.structures.TypeSymbol.SELF_TYPE;
import static cool.semantics.structures.TypeSymbol.illegatInheritableTypes;

public class ASTDefinitionPassVisitor implements ASTVisitor<Void> {
    Scope currentScope = null;

    @Override
    public Void visit(Id id) {
        id.setScope(currentScope);
        return null;
    }

    @Override
    public Void visit(FormalParam param) {
        if (param.name.getText().equals("self")) {
            SymbolTable.error(param.context,
                              param.name,
                              String.format("Method %s of class %s has formal parameter with illegal name self",
                                            ((FunctionSymbol)currentScope).getName(),
                                            ((ClassSymbol)currentScope.getParent()).getName()),
                              param);
            return null;
        }

        var symbol = new IdSymbol(param.name.getText());
        param.setScope((FunctionSymbol) currentScope);
        param.setSymbol(symbol);

        if (!currentScope.add(symbol)) {
            SymbolTable.error(param.context,
                              param.name,
                              String.format("Method %s of class %s redefines formal parameter %s",
                                            ((FunctionSymbol)currentScope).getName(),
                                            ((ClassSymbol)currentScope.getParent()).getName(),
                                            param.name.getText()),
                              param);
            return null;
        }

        if (param.type.getText().equals("SELF_TYPE")) {
            SymbolTable.error(param.context,
                    param.type,
                    String.format("Method %s of class %s has formal parameter %s with illegal type SELF_TYPE",
                            ((FunctionSymbol)currentScope).getName(),
                            ((ClassSymbol)currentScope.getParent()).getName(),
                            param.name.getText()),
                    param);
            return null;
        }

        return null;
    }

    @Override
    public Void visit(Program program) {
        currentScope = SymbolTable.globals;
        program.classes.forEach(classNode -> classNode.accept(this));

        program.classes.forEach(classNode -> {
            if (classNode.getScope() != null && classNode.inherits != null) {
                var inheritedClassScope = currentScope.lookup(classNode.inherits.getText());
                if (inheritedClassScope instanceof ClassSymbol) {
                    classNode.getScope().setInheritedScope((ClassSymbol) inheritedClassScope);
                } else if (!classNode.hasSemanticError()) {
                    SymbolTable.error(classNode.context, classNode.inherits, String.format("Class %s has undefined parent %s", classNode.name.getText(), classNode.inherits.getText()), classNode);
                }
            } else if (classNode.getScope() != null) {
                var objectScope = currentScope.lookup("Object");
                if (objectScope instanceof ClassSymbol) {
                    classNode.getScope().setInheritedScope((ClassSymbol) objectScope);
                }
            }
        });
        return null;
    }

    @Override
    public Void visit(Class myClass) {
        var className = myClass.name.getText();
        if (className.equals(SELF_TYPE.getName())) {
            SymbolTable.error(myClass.context, myClass.name, "Class has illegal name SELF_TYPE", myClass);
            return null;
        }

        var classSymbol = new ClassSymbol(currentScope, className);
        myClass.setScope(classSymbol);
        currentScope = classSymbol;

        if (!currentScope.getParent().add(classSymbol)) {
            SymbolTable.error(myClass.context, myClass.name, String.format("Class %s is redefined", className), myClass);
            currentScope = currentScope.getParent();
            return null;
        }

        if (myClass.inherits != null) {
            var inheritedClassName = myClass.inherits.getText();
            if (illegatInheritableTypes.contains(inheritedClassName)) {
                SymbolTable.error(myClass.context, myClass.inherits, String.format("Class %s has illegal parent %s", className, inheritedClassName), myClass);
                currentScope = currentScope.getParent();
                return null;
            }
        }

        for (var feature : myClass.features) {
            feature.accept(this);
        }

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(VariableDefinition variabileDefinition) {
        if (variabileDefinition.name.getText().equals("self")) {
            SymbolTable.error(variabileDefinition.context,
                              variabileDefinition.name,
                              String.format("Class %s has attribute with illegal name self", ((ClassSymbol)currentScope).getName()),
                              variabileDefinition);
            return null;
        }

        var symbol = new IdSymbol(variabileDefinition.name.getText());
        variabileDefinition.setScope((ClassSymbol)currentScope);
        variabileDefinition.setSymbol(symbol);

        if (!currentScope.add(symbol)) {
            SymbolTable.error(variabileDefinition.context,
                    variabileDefinition.name,
                    String.format("Class %s redefines attribute %s", ((ClassSymbol)currentScope).getName(), variabileDefinition.name.getText()), variabileDefinition);
            return null;
        }

        if (variabileDefinition.variableValue != null) {
            variabileDefinition.variableValue.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(FunctionDefinition functionDefinition) {

        var functionSymbol = new FunctionSymbol((ClassSymbol) currentScope, functionDefinition.name.getText());
        functionDefinition.setScope(functionSymbol);
        currentScope = functionSymbol;

        var classScope = (ClassSymbol) currentScope.getParent();

        if (!currentScope.getParent().add(functionSymbol)) {
            SymbolTable.error(functionDefinition.context,
                    functionDefinition.name,
                    String.format("Class %s redefines method %s",
                                  classScope.getName(),
                                  functionDefinition.name.getText()),
                    functionDefinition);
            currentScope = currentScope.getParent();
            return null;
        }

        if (functionDefinition.formalParams != null) {
            functionDefinition.formalParams.forEach(param -> param.accept(this));
        }

        if (functionDefinition.functionValue != null) {
            functionDefinition.functionValue.accept(this);
        }

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(Int intVal) {
        return null;
    }

    @Override
    public Void visit(StringNode string) {
        return null;
    }

    @Override
    public Void visit(BoolNode bool) {
        return null;
    }

    @Override
    public Void visit(BinaryOperator binaryOperator) {
        binaryOperator.left.accept(this);
        binaryOperator.right.accept(this);
        return null;
    }

    @Override
    public Void visit(UnaryOperator unaryOperator) {
        unaryOperator.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(New newNode) {
        newNode.setScope(currentScope);
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        if (assign.name.getText().equals("self")) {
            SymbolTable.error(assign.context, assign.name, "Cannot assign to self", assign);
            return null;
        }

        assign.setScope(currentScope);
        assign.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(InitCall initCall) {
        return null;
    }

    @Override
    public Void visit(Call call) {
        return null;
    }

    @Override
    public Void visit(If ifNode) {
        ifNode.setScope(currentScope);

        ifNode.cond.accept(this);
        ifNode.thenBranch.accept(this);
        ifNode.elseBranch.accept(this);
        return null;
    }

    @Override
    public Void visit(While whileNode) {
        whileNode.setScope(currentScope);
        whileNode.cond.accept(this);
        whileNode.action.accept(this);
        return null;
    }

    @Override
    public Void visit(LocalParam localParam) {
        if (localParam.name.getText().equals("self")) {
            SymbolTable.error(localParam.context,
                              localParam.name,
                              "Let variable has illegal name self",
                              localParam);
            return null;
        }

        var idSymbol = new IdSymbol(localParam.name.getText());
        localParam.setSymbol(idSymbol);
//        currentScope = new DefaultScope(currentScope);
//        localParam.setScope(currentScope);

        if (localParam.value != null) {
            localParam.value.accept(this);
        }
//        currentScope.add(idSymbol);

        return null;
    }

    @Override
    public Void visit(Let let) {
        // current let branch does not have own definition in his scope, so we have to do this
        var prevParam = let.params.get(0);
        prevParam.setScope(currentScope);
        prevParam.accept(this);
        for (int i = 1; i < let.params.size(); i++) {
            currentScope = new DefaultScope(currentScope);
            if (prevParam.getSymbol() != null) {
                currentScope.add(prevParam.getSymbol());
            }

            var currParam = let.params.get(i);
            currParam.setScope(currentScope);
            currParam.accept(this);
            prevParam = currParam;
        }

        currentScope = new DefaultScope(currentScope);
        currentScope.add(prevParam.getSymbol());

        let.action.accept(this);;

        // rebuild scope
        let.params.forEach(param -> {
            currentScope = currentScope.getParent();
        });
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
        if (caseBranch.name.getText().equals("self")) {
            SymbolTable.error(caseBranch.context, caseBranch.name, "Case variable has illegal name self", caseBranch);
        }

        if (caseBranch.type.getText().equals("SELF_TYPE")) {
            SymbolTable.error(caseBranch.context, caseBranch.type, String.format("Case variable %s has illegal type SELF_TYPE", caseBranch.name.getText()), caseBranch);
        }

        caseBranch.setScope(currentScope);
        caseBranch.value.accept(this);
        return null;
    }

    @Override
    public Void visit(Case caseNode) {
        caseNode.caseExpr.accept(this);
        caseNode.caseBranches.forEach(branch -> branch.accept(this));
        return null;
    }

    @Override
    public Void visit(Block block) {
        block.actions.forEach(action -> action.accept(this));
        return null;
    }
}
