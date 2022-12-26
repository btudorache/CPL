package cool.semantics;

import cool.ast.ASTVisitor;
import cool.ast.nodes.Class;
import cool.ast.nodes.*;
import cool.semantics.structures.*;

public class ASTDefinitionPassVisitor implements ASTVisitor<Void> {
    Scope currentScope = null;
    GlobalScope globalScope = null;

    public GlobalScope getGlobalScope() {
        return globalScope;
    }

    @Override
    public Void visit(Id id) {
        id.scope = currentScope;
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

        var symbol = new IdSymbol(param.name.getText(), param.type.getText());
        param.globalScope = globalScope;
        param.scope = (FunctionSymbol) currentScope;
        param.symbol = symbol;

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

        var definedParamType = globalScope.lookupType(param.type.getText());
        if (definedParamType != null) {
            param.symbol.type = definedParamType;
        }

        return null;
    }

    @Override
    public Void visit(Program program) {
        currentScope = SymbolTable.globals;
        globalScope = SymbolTable.globals;
        program.classes.forEach(classNode -> classNode.accept(this));

        program.classes.forEach(classNode -> {
            if (classNode.scope != null && classNode.inherits != null) {
                var inheritedClassScope = currentScope.lookup(classNode.inherits.getText());
                if (inheritedClassScope instanceof ClassSymbol) {
                    classNode.scope.setInheritedScope((ClassSymbol) inheritedClassScope);
                } else if (!classNode.hasSemanticError()) {
                    SymbolTable.error(classNode.context, classNode.inherits, String.format("Class %s has undefined parent %s", classNode.name.getText(), classNode.inherits.getText()), classNode);
                }
            } else if (classNode.scope != null) {
                var objectScope = currentScope.lookup("Object");
                if (objectScope instanceof ClassSymbol) {
                    classNode.scope.setInheritedScope((ClassSymbol) objectScope);
                }
            }
        });

//        var mainClass = globalScope.lookupType("Main");
//        if (mainClass == null) {
//            SymbolTable.error(program.context, program.context.start, "No method main in class Main", program);
//            return null;
//        }
//
//        var mainFunction = mainClass.lookup("main");
//        if (!(mainFunction instanceof FunctionSymbol)) {
//            SymbolTable.error(program.context, program.context.start, "No method main in class Main", program);
//            return null;
//        }

        return null;
    }

    @Override
    public Void visit(Class myClass) {
        var className = myClass.name.getText();
        if (className.equals(ClassSymbol.SELF_TYPE.getName())) {
            SymbolTable.error(myClass.context, myClass.name, "Class has illegal name SELF_TYPE", myClass);
            return null;
        }

        var classSymbol = new ClassSymbol(currentScope, className);
        myClass.scope = classSymbol;
        currentScope = classSymbol;

        if (!currentScope.getParent().add(classSymbol)) {
            SymbolTable.error(myClass.context, myClass.name, String.format("Class %s is redefined", className), myClass);
            currentScope = currentScope.getParent();
            return null;
        }

        if (myClass.inherits != null) {
            var inheritedClassName = myClass.inherits.getText();
            if (ClassSymbol.illegalInheritableTypes.contains(inheritedClassName)) {
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
    public Void visit(VariableDefinition variableDefinition) {
        if (variableDefinition.name.getText().equals("self")) {
            SymbolTable.error(variableDefinition.context,
                              variableDefinition.name,
                              String.format("Class %s has attribute with illegal name self", ((ClassSymbol)currentScope).getName()),
                              variableDefinition);
            return null;
        }

        var symbol = new IdSymbol(variableDefinition.name.getText(), variableDefinition.type.getText());
        variableDefinition.scope = (ClassSymbol) currentScope;
        variableDefinition.globalScope = globalScope;
        variableDefinition.symbol = symbol;

        if (!currentScope.add(symbol)) {
            SymbolTable.error(variableDefinition.context,
                    variableDefinition.name,
                    String.format("Class %s redefines attribute %s", ((ClassSymbol)currentScope).getName(), variableDefinition.name.getText()), variableDefinition);
            return null;
        }

        if (variableDefinition.variableValue != null) {
            variableDefinition.variableValue.accept(this);
        }

        var definedParamType = globalScope.lookupType(variableDefinition.type.getText());
        if (definedParamType != null) {
            variableDefinition.symbol.type = definedParamType;
        }

        return null;
    }

    @Override
    public Void visit(FunctionDefinition functionDefinition) {

        var functionSymbol = new FunctionSymbol((ClassSymbol) currentScope, functionDefinition.name.getText());
        functionSymbol.typeString = functionDefinition.type.getText();
        functionDefinition.scope = functionSymbol;
        functionDefinition.globalScope = globalScope;
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
        newNode.scope = currentScope;
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        if (assign.name.getText().equals("self")) {
            SymbolTable.error(assign.context, assign.name, "Cannot assign to self", assign);
            return null;
        }

        assign.scope = currentScope;
        assign.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(ImplicitCall implicitCall) {
        implicitCall.scope = currentScope;
        implicitCall.globalScope = globalScope;

        if (implicitCall.args != null) {
            implicitCall.args.forEach(arg -> arg.accept(this));
        }
        return null;
    }

    @Override
    public Void visit(Call call) {
        call.scope = currentScope;
        call.globalScope = globalScope;

        call.prefix.accept(this);
        if (call.args != null) {
            call.args.forEach(arg -> arg.accept(this));
        }
        return null;
    }

    @Override
    public Void visit(If ifNode) {
        ifNode.scope = currentScope;

        ifNode.cond.accept(this);
        ifNode.thenBranch.accept(this);
        ifNode.elseBranch.accept(this);
        return null;
    }

    @Override
    public Void visit(While whileNode) {
        whileNode.scope = currentScope;
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

        localParam.symbol = new IdSymbol(localParam.name.getText(), localParam.type.getText());
        localParam.globalScope = globalScope;

        if (localParam.value != null) {
            localParam.value.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(Let let) {
        // current let branch does not have own definition in his scope, so we have to do this
        var prevParam = let.params.get(0);
        prevParam.scope = currentScope;
        prevParam.accept(this);
        for (int i = 1; i < let.params.size(); i++) {
            currentScope = new DefaultScope(currentScope);
            if (prevParam.symbol != null) {
                currentScope.add(prevParam.symbol);
            }

            var currParam = let.params.get(i);
            currParam.scope = currentScope;
            currParam.accept(this);
            prevParam = currParam;
        }

        currentScope = new DefaultScope(currentScope);
        currentScope.add(prevParam.symbol);

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

        currentScope = new DefaultScope(currentScope);
        currentScope.add(new IdSymbol(caseBranch.name.getText(), caseBranch.type.getText()));
        caseBranch.scope = currentScope;
        caseBranch.value.accept(this);

        currentScope = currentScope.getParent();
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
