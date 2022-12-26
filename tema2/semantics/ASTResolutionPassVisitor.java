package cool.semantics;

import cool.ast.ASTVisitor;
import cool.ast.nodes.Class;
import cool.ast.nodes.*;
import cool.semantics.structures.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ASTResolutionPassVisitor implements ASTVisitor<ResolutionResult> {
    public GlobalScope globalScope;

    public ASTResolutionPassVisitor(GlobalScope globalScope) {
        this.globalScope = globalScope;
    }

    @Override
    public ResolutionResult visit(Id id) {
        if (id.name.getText().equals("self")) {
            return new ResolutionResult(ClassSymbol.SELF_TYPE, id.name, true);
        }

        var resolvedVariableSymbol = id.scope.lookup(id.name.getText());
        if (!(resolvedVariableSymbol instanceof IdSymbol)) {
            var classScope = id.scope.lookupClass();
            var inheritedVariableSymbol = classScope.lookupInheritanceTree(id.name.getText());
            if (!(inheritedVariableSymbol instanceof IdSymbol)) {
                SymbolTable.error(id.context, id.name, String.format("Undefined identifier %s", id.name.getText()), id);
                return null;
            }

            return new ResolutionResult(((IdSymbol)inheritedVariableSymbol).type, id.name);
        }

        return new ResolutionResult(((IdSymbol)resolvedVariableSymbol).type, id.name);
    }

    @Override
    public ResolutionResult visit(FormalParam param) {
        if (!param.hasSemanticError()) {
            var definedParamType = param.globalScope.lookupType(param.type.getText());
            if (definedParamType == null) {
                SymbolTable.error(param.context,
                        param.type,
                        String.format("Method %s of class %s has formal parameter %s with undefined type %s",
                                      param.scope.getName(),
                                      ((ClassSymbol)param.scope.getParent()).getName(),
                                      param.name.getText(),
                                      param.type.getText()),
                        param);
                return null;
            }

            param.symbol.type = definedParamType;
        }
        return null;
    }

    @Override
    public ResolutionResult visit(Program program) {
        ResolutionResult lastType = null;
        for (var classNode : program.classes) {
            lastType = classNode.accept(this);   
        }

        return lastType;
    }

    @Override
    public ResolutionResult visit(Class myClass) {
        // check for cycles
        if (!myClass.hasSemanticError() && myClass.inherits != null) {
            var hasCycles = false;
            List<String> visitedClasses = new ArrayList<>();
            visitedClasses.add(myClass.name.getText());

            var classTraveller = myClass.scope.getInheritedScope();
            while (classTraveller != null && !hasCycles) {
                if (classTraveller.getInheritedScope() != null && !visitedClasses.contains(classTraveller.getInheritedScope().getName())) {
                    visitedClasses.add(classTraveller.getInheritedScope().getName());
                    if (classTraveller.getName().equals(myClass.name.getText())) {
                        hasCycles = true;
                    }
                }
                classTraveller = classTraveller.getInheritedScope();
            }

            if (hasCycles) {
                SymbolTable.error(myClass.context, myClass.name, String.format("Inheritance cycle for class %s", myClass.name.getText()), myClass);
                return null;
            }
        }

        myClass.features.forEach(feature -> feature.accept(this));

        return new ResolutionResult(myClass.scope, myClass.name);
    }

    @Override
    public ResolutionResult visit(VariableDefinition variableDefinition) {
        if (!variableDefinition.hasSemanticError()) {
            if (variableDefinition.scope != null) {
                var inheritedVariableSymbol = variableDefinition.scope.lookupInheritanceTree(variableDefinition.name.getText());
                if (inheritedVariableSymbol instanceof IdSymbol) {
                    SymbolTable.error(variableDefinition.context,
                            variableDefinition.name,
                            String.format("Class %s redefines inherited attribute %s", variableDefinition.scope.getName(), variableDefinition.name.getText()),
                            variableDefinition);
                    return null;
                }

                var variableType = variableDefinition.globalScope.lookupType(variableDefinition.type.getText());
                if (variableType == null) {
                    SymbolTable.error(variableDefinition.context,
                                      variableDefinition.type,
                                      String.format("Class %s has attribute %s with undefined type %s",
                                                    variableDefinition.scope.getName(),
                                                    variableDefinition.name.getText(),
                                                    variableDefinition.type.getText()),
                                      variableDefinition);
                    return null;
                }
                variableDefinition.symbol.type = variableType;

                if (variableDefinition.variableValue != null) {
                    var resolutionResult = variableDefinition.variableValue.accept(this);
                    var valueType = (resolutionResult != null) ? resolutionResult.typeSymbol : null;
                    if (valueType != null) {
                        variableDefinition.symbol.dynamicType = valueType;
                        if (variableType.equals(ClassSymbol.SELF_TYPE)) {
                            return new ResolutionResult(valueType, variableDefinition.name);
                        }

                        if (!variableType.compareType(valueType)) {
                            SymbolTable.error(variableDefinition.context,
                                    resolutionResult.additionalInfo,
                                    String.format("Type %s of initialization expression of attribute %s is incompatible with declared type %s",
                                            valueType.getName(),
                                            variableDefinition.name.getText(),
                                            variableDefinition.type.getText()),
                                    variableDefinition);
                            return null;
                        }
                    }

                    return null;
                }

                return new ResolutionResult(variableType, variableDefinition.name);
            }
        }

        return null;
    }

    @Override
    public ResolutionResult visit(FunctionDefinition functionDefinition) {
        if (!functionDefinition.hasSemanticError()) {
            var classScope = (ClassSymbol) functionDefinition.scope.getParent();
            var returnTypeDefinedSymbol = functionDefinition.globalScope.lookupType(functionDefinition.type.getText());
            if (returnTypeDefinedSymbol == null) {
                SymbolTable.error(functionDefinition.context,
                        functionDefinition.type,
                        String.format("Class %s has method %s with undefined return type %s",
                                classScope.getName(),
                                functionDefinition.name.getText(),
                                functionDefinition.type.getText()),
                        functionDefinition);
                return null;
            }
            functionDefinition.scope.type = returnTypeDefinedSymbol;

            if (functionDefinition.formalParams != null) {
                functionDefinition.formalParams.forEach(param -> param.accept(this));
            }

            var overloadedMethodSymbol = classScope.lookupInheritanceTree(functionDefinition.name.getText());
            if (overloadedMethodSymbol instanceof FunctionSymbol) {
                var params = ((FunctionSymbol) overloadedMethodSymbol).getFormals();
                if (params.size() != functionDefinition.formalParams.size()) {
                    SymbolTable.error(functionDefinition.context,
                            functionDefinition.name,
                            String.format("Class %s overrides method %s with different number of formal parameters",
                                          classScope.getName(),
                                          functionDefinition.name.getText()),
                            functionDefinition);
                    return null;
                }

                var overloadedParams = new LinkedList(Arrays.asList(params.values().toArray()));
                for (int i = 0; i < params.size(); i++) {
                    var overloadedParam = (IdSymbol)overloadedParams.get(i);
                    var currentParam = functionDefinition.formalParams.get(i);

                    if (!overloadedParam.type.getName().equals(currentParam.type.getText())) {
                        SymbolTable.error(functionDefinition.context,
                                          currentParam.type,
                                          String.format("Class %s overrides method %s but changes type of formal parameter %s from %s to %s",
                                                        classScope.getName(),
                                                        functionDefinition.name.getText(),
                                                        currentParam.name.getText(),
                                                        overloadedParam.type.getName(),
                                                        currentParam.type.getText()),
                                          functionDefinition);
                        return null;
                    }
                }

                var functionType = functionDefinition.globalScope.lookupType(((FunctionSymbol) overloadedMethodSymbol).typeString);
                if (functionType != null) {
                    ((FunctionSymbol) overloadedMethodSymbol).type = functionType;
                }

                var overloadedMethodType = ((FunctionSymbol) overloadedMethodSymbol).type.getName();
                if (!overloadedMethodType.equals(functionDefinition.type.getText())) {
                    SymbolTable.error(functionDefinition.context,
                                      functionDefinition.type,
                                      String.format("Class %s overrides method %s but changes return type from %s to %s",
                                                    classScope.getName(),
                                                    functionDefinition.name.getText(),
                                                    overloadedMethodType,
                                                    functionDefinition.type.getText()),
                                      functionDefinition);
                    return null;
                }
            }

            if (functionDefinition.functionValue != null) {
                var resolutionResult = functionDefinition.functionValue.accept(this);
                var functionValueType = (resolutionResult != null ) ? resolutionResult.typeSymbol : null;


                if (functionValueType != null) {
                    if (functionValueType.equals(ClassSymbol.SELF_TYPE) && !returnTypeDefinedSymbol.equals(ClassSymbol.SELF_TYPE)) {
                        functionValueType = functionDefinition.scope.lookupClass();
                    }

                    if (!returnTypeDefinedSymbol.compareType(functionValueType)) {
                        SymbolTable.error(functionDefinition.context,
                                resolutionResult.additionalInfo,
                                String.format("Type %s of the body of method %s is incompatible with declared return type %s",
                                        functionValueType.getName(),
                                        functionDefinition.name.getText(),
                                        functionDefinition.type.getText()),
                                functionDefinition);
                        return null;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public ResolutionResult visit(Int intVal) {
        return new ResolutionResult(ClassSymbol.INT, intVal.name);
    }

    @Override
    public ResolutionResult visit(StringNode string) {
        return new ResolutionResult(ClassSymbol.STRING, string.name);
    }

    @Override
    public ResolutionResult visit(BoolNode bool) {
        return new ResolutionResult(ClassSymbol.BOOL, bool.name);
    }

    @Override
    public ResolutionResult visit(BinaryOperator binaryOperator) {
        var leftResolutionResult = binaryOperator.left.accept(this);
        var leftType = (leftResolutionResult != null) ? leftResolutionResult.typeSymbol : null;
        var rightResolutionResult = binaryOperator.right.accept(this);
        var rightType = (rightResolutionResult != null ) ? rightResolutionResult.typeSymbol : null;

        var operandsExpectingInt = List.of("+", "-", "*", "/", "<", "<=");
        var relationalOperands = List.of("<", "<=", "=");
        if (operandsExpectingInt.contains(binaryOperator.operator.getText())) {
            if (rightType != null && !rightType.getName().equals(ClassSymbol.INT.getName())) {
                SymbolTable.error(binaryOperator.context, rightResolutionResult.additionalInfo, String.format("Operand of %s has type %s instead of Int", binaryOperator.operator.getText(), rightType.getName()), binaryOperator);
                return null;
            }

            if (leftType != null && !leftType.getName().equals(ClassSymbol.INT.getName())) {
                SymbolTable.error(binaryOperator.context, leftResolutionResult.additionalInfo, String.format("Operand of %s has type %s instead of Int", binaryOperator.operator.getText(), leftType.getName()), binaryOperator);
                return null;
            }

//            if (leftType.getName().eq)

            if (relationalOperands.contains(binaryOperator.operator.getText())) {
                return new ResolutionResult(ClassSymbol.BOOL, binaryOperator.context.start);
            } else {
                return new ResolutionResult(ClassSymbol.INT, binaryOperator.context.start);
            }
        }

        var permittedEqualityTypes = List.of("Int", "String", "Bool");
        if (rightType != null && leftType != null) {
            if (binaryOperator.operator.getText().equals("=") && permittedEqualityTypes.contains(rightType.getName()) && !rightType.getName().equals(leftType.getName())) {
                SymbolTable.error(binaryOperator.context, binaryOperator.operator, String.format("Cannot compare %s with %s", leftType.getName(), rightType.getName()), binaryOperator);
                return null;
            } else if (binaryOperator.operator.getText().equals("=") && rightType.getName().equals(leftType.getName())) {
                return new ResolutionResult(ClassSymbol.BOOL, leftResolutionResult.additionalInfo);
            }
        }

        return null;
    }

    @Override
    public ResolutionResult visit(UnaryOperator unaryOperator) {
        var exprResolutionResult = unaryOperator.expr.accept(this);
        var exprType = (exprResolutionResult != null) ? exprResolutionResult.typeSymbol : null;

        var operandsExpectingInt = List.of("~", "-");
        if (operandsExpectingInt.contains(unaryOperator.operator.getText())
            && exprType != null && !exprType.getName().equals(ClassSymbol.INT.getName())) {
            SymbolTable.error(unaryOperator.context, exprResolutionResult.additionalInfo, String.format("Operand of %s has type %s instead of Int", unaryOperator.operator.getText(), exprType.getName()), unaryOperator);
            return null;
        }

        if (unaryOperator.operator.getText().equals("not")
            && exprType != null && !exprType.getName().equals(ClassSymbol.BOOL.getName())) {
            SymbolTable.error(unaryOperator.context, exprResolutionResult.additionalInfo, String.format("Operand of not has type %s instead of Bool", exprType.getName()), unaryOperator);
            return null;
        }

        if (unaryOperator.operator.getText().equals("isvoid")) {
            return new ResolutionResult(ClassSymbol.BOOL, (exprResolutionResult != null) ? exprResolutionResult.additionalInfo : unaryOperator.context.start);
        }

        return new ResolutionResult(exprType, (exprResolutionResult != null) ? exprResolutionResult.additionalInfo : unaryOperator.context.start);
    }

    @Override
    public ResolutionResult visit(New newNode) {
        var definedType = newNode.scope.lookup(newNode.type.getText());
        if (!(definedType instanceof ClassSymbol)) {
            SymbolTable.error(newNode.context, newNode.type, String.format("new is used with undefined type %s", newNode.type.getText()), newNode);
            return null;
        }

        return new ResolutionResult((ClassSymbol) definedType, newNode.context.start);
    }

    @Override
    public ResolutionResult visit(Assign assign) {
        if (!assign.hasSemanticError()) {
            var definedVar = (IdSymbol)assign.scope.lookup(assign.name.getText());
            var definedType = (definedVar != null) ? definedVar.type : null;
            var resolutionResult = assign.expr.accept(this);
            var exprType = (resolutionResult != null) ? resolutionResult.typeSymbol : null;

            if (definedType != null && exprType != null) {
                if (!definedType.compareType(exprType)) {
                    SymbolTable.error(assign.context, resolutionResult.additionalInfo, String.format("Type %s of assigned expression is incompatible with declared type %s of identifier %s", exprType.getName(), definedType.getName(), assign.name.getText()), assign);
                    return null;
                }

                return new ResolutionResult(exprType, assign.context.start);
            }

            return null;
        }

        return null;
    }

    @Override
    public ResolutionResult visit(ImplicitCall implicitCall) {
        ClassSymbol classScope = implicitCall.scope.lookupClass();

        Symbol functionToken = classScope.lookup(implicitCall.name.getText());
        Symbol inheritedFunctionToken = classScope.lookupInheritanceTree(implicitCall.name.getText());
        if (!(functionToken instanceof FunctionSymbol) && !(inheritedFunctionToken instanceof FunctionSymbol)) {
            SymbolTable.error(implicitCall.context, implicitCall.name, String.format("Undefined method %s in class %s", implicitCall.name.getText(), classScope.getName()), implicitCall);
            return null;
        }

        FunctionSymbol castedFunctionToken = (!(functionToken instanceof FunctionSymbol)) ? (FunctionSymbol) inheritedFunctionToken : (FunctionSymbol) functionToken;
        ClassSymbol functionType = implicitCall.globalScope.lookupType(castedFunctionToken.typeString);
        if (functionType != null) {
            castedFunctionToken.type = functionType;
        }

        var returnedType = (castedFunctionToken.type.equals(ClassSymbol.SELF_TYPE)) ? classScope : castedFunctionToken.type;
        if (castedFunctionToken.getFormals().size() != implicitCall.args.size()) {
            SymbolTable.error(implicitCall.context, implicitCall.context.start, String.format("Method %s of class %s is applied to wrong number of arguments", implicitCall.name.getText(), classScope.getName()), implicitCall);
            return new ResolutionResult(returnedType, implicitCall.context.start);
        }

        var params = new LinkedList(Arrays.asList(castedFunctionToken.getFormals().values().toArray()));
        for (int i = 0; i < params.size(); i++) {
            var definedParam = (IdSymbol) params.get(i);
            var definedParamType = implicitCall.globalScope.lookupType(definedParam.typeString);
            if (definedParamType != null) {
                definedParam.type = definedParamType;
            }

            var actualParamResolution = implicitCall.args.get(i).accept(this);
            var actualParamType = (actualParamResolution != null) ? actualParamResolution.typeSymbol : null;
            if (actualParamType != null && actualParamType.equals(ClassSymbol.SELF_TYPE)) {
                actualParamType = implicitCall.scope.lookupClass();
            }

            if (!definedParam.type.compareType(actualParamType)) {
                SymbolTable.error(implicitCall.context,
                        implicitCall.args.get(i).context.start,
                        String.format("In call to method %s of class %s, actual type %s of formal parameter %s is incompatible with declared type %s",
                                      castedFunctionToken.getName(),
                                      classScope.getName(),
                                      actualParamType.getName(),
                                      definedParam.getName(),
                                      definedParam.type.getName()),
                        implicitCall);
                return new ResolutionResult(returnedType, implicitCall.context.start);
            }
        }

        return new ResolutionResult(returnedType, implicitCall.context.start);
    }

    @Override
    public ResolutionResult visit(Call call) {
        var resolutionResult = call.prefix.accept(this);
        var classScope = (resolutionResult != null) ? resolutionResult.typeSymbol : null;

        if (classScope != null) {
            if (classScope.equals(ClassSymbol.SELF_TYPE)) {
                classScope = call.scope.lookupClass();
            }

            if (call.atType == null) {
                Symbol functionToken = classScope.lookup(call.name.getText());
                Symbol inheritedFunctionToken = classScope.lookupInheritanceTree(call.name.getText());

                if (!(functionToken instanceof FunctionSymbol) && !(inheritedFunctionToken instanceof FunctionSymbol)) {
                    SymbolTable.error(call.context, call.name, String.format("Undefined method %s in class %s", call.name.getText(), classScope.getName()), call);
                    return null;
                }


                FunctionSymbol castedFunctionToken = (!(functionToken instanceof FunctionSymbol)) ? (FunctionSymbol) inheritedFunctionToken : (FunctionSymbol) functionToken;
                ClassSymbol functionType = call.globalScope.lookupType(castedFunctionToken.typeString);
                if (functionType != null) {
                    castedFunctionToken.type = functionType;
                }

                var returnedType = (castedFunctionToken.type.equals(ClassSymbol.SELF_TYPE)) ? classScope : castedFunctionToken.type;
                if (resolutionResult.selfDispatch) {
                    returnedType = ClassSymbol.SELF_TYPE;
                }

                if (castedFunctionToken.getFormals().size() != call.args.size()) {
                    SymbolTable.error(call.context, call.name, String.format("Method %s of class %s is applied to wrong number of arguments", call.name.getText(), classScope.getName()), call);
                    return new ResolutionResult(returnedType, call.context.start);
                }

                var params = new LinkedList(Arrays.asList(castedFunctionToken.getFormals().values().toArray()));
                for (int i = 0; i < params.size(); i++) {
                    var definedParam = (IdSymbol) params.get(i);
                    var definedParamType = call.globalScope.lookupType(definedParam.typeString);
                    if (definedParamType != null) {
                        definedParam.type = definedParamType;
                    }

                    var actualParamResolution = call.args.get(i).accept(this);
                    var actualParamType = (actualParamResolution != null) ? actualParamResolution.typeSymbol : null;
                    if (actualParamType != null && actualParamType.equals(ClassSymbol.SELF_TYPE)) {
                        actualParamType = call.scope.lookupClass();
                    }

                    if (!definedParam.type.compareType(actualParamType)) {
                        SymbolTable.error(call.context,
                                call.args.get(i).context.start,
                                String.format("In call to method %s of class %s, actual type %s of formal parameter %s is incompatible with declared type %s",
                                        castedFunctionToken.getName(),
                                        classScope.getName(),
                                        actualParamType.getName(),
                                        definedParam.getName(),
                                        definedParam.type.getName()),
                                call);
                        return new ResolutionResult(returnedType, call.context.start, resolutionResult.selfDispatch);
                    }
                }

                return new ResolutionResult(returnedType, call.context.start, resolutionResult.selfDispatch);
            } else {
                if (call.atType.getText().equals(ClassSymbol.SELF_TYPE.getName())) {
                    SymbolTable.error(call.context, call.atType, "Type of static dispatch cannot be SELF_TYPE", call);
                    return null;
                }

                var staticType = call.globalScope.lookupType(call.atType.getText());
                if (staticType == null) {
                    SymbolTable.error(call.context, call.atType, String.format("Type %s of static dispatch is undefined", call.atType.getText()), call);
                    return null;
                }

                var comparedType = classScope.equals(ClassSymbol.SELF_TYPE) ? call.scope.lookupClass() : classScope;
                if (!staticType.compareType(comparedType)) {
                    SymbolTable.error(call.context, call.atType, String.format("Type %s of static dispatch is not a superclass of type %s", call.atType.getText(), classScope.getName()), call);
                    return null;
                }

                Symbol functionToken = staticType.lookup(call.name.getText());
                Symbol inheritedFunctionToken = staticType.lookupInheritanceTree(call.name.getText());
                if (!(functionToken instanceof FunctionSymbol) && !(inheritedFunctionToken instanceof FunctionSymbol)) {
                    SymbolTable.error(call.context, call.name, String.format("Undefined method %s in class %s", call.name.getText(), staticType.getName()), call);
                    return null;
                }

                FunctionSymbol castedFunctionToken = (!(functionToken instanceof FunctionSymbol)) ? (FunctionSymbol) inheritedFunctionToken : (FunctionSymbol) functionToken;
                ClassSymbol functionType = call.globalScope.lookupType(castedFunctionToken.typeString);
                if (functionType != null) {
                    castedFunctionToken.type = functionType;
                }

                var returnedType = (castedFunctionToken.type.equals(ClassSymbol.SELF_TYPE)) ? classScope : castedFunctionToken.type;
                if (resolutionResult.selfDispatch) {
                    returnedType = ClassSymbol.SELF_TYPE;
                }


                if (castedFunctionToken.getFormals().size() != call.args.size()) {
                    SymbolTable.error(call.context, call.atType, String.format("Method %s of class %s is applied to wrong number of arguments", call.name.getText(), staticType.getName()), call);
                    return new ResolutionResult(returnedType, call.context.start, resolutionResult.selfDispatch);
                }

                var params = new LinkedList(Arrays.asList(castedFunctionToken.getFormals().values().toArray()));
                for (int i = 0; i < params.size(); i++) {
                    var definedParam = (IdSymbol) params.get(i);
                    var definedParamType = call.globalScope.lookupType(definedParam.typeString);
                    if (definedParamType != null) {
                        definedParam.type = definedParamType;
                    }

                    var actualParamResolution = call.args.get(i).accept(this);
                    var actualParamType = (actualParamResolution != null) ? actualParamResolution.typeSymbol : null;
                    if (actualParamType != null && actualParamType.equals(ClassSymbol.SELF_TYPE)) {
                        actualParamType = call.scope.lookupClass();
                    }

                    if (!definedParam.type.compareType(actualParamType)) {
                        SymbolTable.error(call.context,
                                call.args.get(i).context.start,
                                String.format("In call to method %s of class %s, actual type %s of formal parameter %s is incompatible with declared type %s",
                                        castedFunctionToken.getName(),
                                        staticType.getName(),
                                        actualParamType.getName(),
                                        definedParam.getName(),
                                        definedParam.type.getName()),
                                call);
                        return new ResolutionResult(returnedType, call.context.start, resolutionResult.selfDispatch);
                    }
                }

                return new ResolutionResult(returnedType, call.context.start, resolutionResult.selfDispatch);
            }
        }

        return null;
    }

    @Override
    public ResolutionResult visit(If ifNode) {
        var conditionResolutionResult = ifNode.cond.accept(this);
        var conditionType = (conditionResolutionResult != null) ? conditionResolutionResult.typeSymbol : null;
        if (conditionType != null && !ClassSymbol.BOOL.compareType(conditionType)) {
            SymbolTable.error(ifNode.context, conditionResolutionResult.additionalInfo, String.format("If condition has type %s instead of Bool", conditionType.getName()), ifNode);
            return new ResolutionResult(ClassSymbol.OBJECT, ifNode.context.start);
        }

        var thenResolutionResult = ifNode.thenBranch.accept(this);
        var thenType = (thenResolutionResult != null) ? thenResolutionResult.typeSymbol : null;

        var elseResolutionResult =ifNode.elseBranch.accept(this);
        var elseType = (elseResolutionResult != null) ? elseResolutionResult.typeSymbol : null;

        if (thenType != null && elseType != null) {
            if (thenType.equals(ClassSymbol.SELF_TYPE)) {
                thenType = ifNode.scope.lookupClass();
            }

            if (elseType.equals(ClassSymbol.SELF_TYPE)) {
                elseType = ifNode.scope.lookupClass();
            }
            return new ResolutionResult(ClassSymbol.LCA(thenType, elseType), ifNode.context.start);
        }

        return null;
    }

    @Override
    public ResolutionResult visit(While whileNode) {
        var conditionResolutionResult = whileNode.cond.accept(this);
        var conditionType = (conditionResolutionResult != null) ? conditionResolutionResult.typeSymbol : null;
        if (conditionType != null && !ClassSymbol.BOOL.compareType(conditionType)) {
            SymbolTable.error(whileNode.context, conditionResolutionResult.additionalInfo, String.format("While condition has type %s instead of Bool", conditionType.getName()), whileNode);
            return new ResolutionResult(ClassSymbol.OBJECT, whileNode.context.start);
        }

        whileNode.action.accept(this);
        return new ResolutionResult(ClassSymbol.OBJECT, whileNode.context.start);
    }

    @Override
    public ResolutionResult visit(LocalParam localParam) {
        if (!localParam.hasSemanticError()) {
            var definedType = localParam.globalScope.lookupType(localParam.type.getText());
            if (definedType == null) {
                SymbolTable.error(
                        localParam.context,
                        localParam.type,
                        String.format(
                                "Let variable %s has undefined type %s",
                                localParam.name.getText(),
                                localParam.type.getText()
                        ),
                        localParam
                );
                return null;
            }
            localParam.symbol.type = definedType;

            if (localParam.value != null) {
                var resolutionResult = localParam.value.accept(this);
                var valueType = (resolutionResult != null) ? resolutionResult.typeSymbol : null;
                if (valueType != null && !definedType.compareType(valueType)) {
                    SymbolTable.error(
                            localParam.context,
                            resolutionResult.additionalInfo,
                            String.format(
                                    "Type %s of initialization expression of identifier %s is incompatible with declared type %s",
                                    valueType.getName(),
                                    localParam.name.getText(),
                                    localParam.type.getText()
                            ),
                            localParam
                    );
                    return null;
                }

                localParam.symbol.type = definedType;
                localParam.symbol.dynamicType = valueType;
                return new ResolutionResult(valueType, localParam.name);
            }
        }

        return null;
    }

    @Override
    public ResolutionResult visit(Let let) {
        let.params.forEach(param -> param.accept(this));
        var actionResolution = let.action.accept(this);
        var actionType = (actionResolution != null) ? actionResolution.typeSymbol : null;

        if (actionType != null) {
            return new ResolutionResult(actionType, let.context.start);
        }
        return null;
    }

    @Override
    public ResolutionResult visit(CaseBranch caseBranch) {
        if (!caseBranch.hasSemanticError()) {
            var definedCaseType = caseBranch.scope.lookup(caseBranch.type.getText());
            if (!(definedCaseType instanceof ClassSymbol)) {
                SymbolTable.error(caseBranch.context, caseBranch.type, String.format("Case variable %s has undefined type %s", caseBranch.name.getText(), caseBranch.type.getText()), caseBranch);
                return null;
            }

            var idSymbol = (IdSymbol) caseBranch.scope.lookup(caseBranch.name.getText());
            idSymbol.type = (ClassSymbol) definedCaseType;

            var exprResolution = caseBranch.value.accept(this);
            var exprType = (exprResolution != null) ? exprResolution.typeSymbol : null;
            return new ResolutionResult(exprType, caseBranch.context.start);
        }

        return null;
    }

    @Override
    public ResolutionResult visit(Case caseNode) {
        caseNode.caseExpr.accept(this);
        ClassSymbol typeSymbol = null;
        for (var branch : caseNode.caseBranches) {
            var branchResolution = branch.accept(this);
            var branchType = (branchResolution != null) ? branchResolution.typeSymbol : null;
            if (branchType != null) {
                if (typeSymbol == null) {
                    typeSymbol = branchType;
                } else {
                    typeSymbol = ClassSymbol.LCA(typeSymbol, branchType);
                }
            }
        }

        if (typeSymbol != null) {
            return new ResolutionResult(typeSymbol, caseNode.context.start);
        }
        return null;
    }

    @Override
    public ResolutionResult visit(Block block) {
        ClassSymbol blockType = null;
        for (var action : block.actions) {
            var actionResolution = action.accept(this);
            var actionType = (actionResolution != null) ? actionResolution.typeSymbol : null;
            if (actionType != null) {
                blockType = actionType;
            }
        }

        if (blockType != null) {
            return new ResolutionResult(blockType, block.context.start);
        }

        return null;
    }
}
