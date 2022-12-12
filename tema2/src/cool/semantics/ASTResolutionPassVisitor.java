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
    @Override
    public ResolutionResult visit(Id id) {
        if (id.name.getText().equals("self")) {
            return new ResolutionResult(id.getScope().lookupClass(), id.name);
        }

        var resolvedVariableSymbol = id.getScope().lookup(id.name.getText());
        if (!(resolvedVariableSymbol instanceof IdSymbol)) {
            SymbolTable.error(id.context, id.name, String.format("Undefined identifier %s", id.name.getText()), id);
            return null;
        }

        return new ResolutionResult(((IdSymbol)resolvedVariableSymbol).getType(), id.name);
    }

    @Override
    public ResolutionResult visit(FormalParam param) {
        if (!param.hasSemanticError()) {
            var definedParamType = param.getScope().lookup(param.type.getText());
            if (!(definedParamType instanceof TypeSymbol)) {
                SymbolTable.error(param.context,
                        param.type,
                        String.format("Method %s of class %s has formal parameter %s with undefined type %s",
                                      param.getScope().getName(),
                                      ((ClassSymbol)param.getScope().getParent()).getName(),
                                      param.name.getText(),
                                      param.type.getText()),
                        param);
                return null;
            }

            param.getSymbol().setType((TypeSymbol) definedParamType);
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

            var classTraveller = myClass.getScope().getInheritedScope();
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

        return new ResolutionResult(myClass.getScope(), myClass.name);
    }

    @Override
    public ResolutionResult visit(VariableDefinition variabileDefinition) {
        if (!variabileDefinition.hasSemanticError()) {
            if (variabileDefinition.getScope() != null) {
                var inheritedVariableSymbol = variabileDefinition.getScope().lookupInheritanceTree(variabileDefinition.name.getText());
                if (inheritedVariableSymbol instanceof IdSymbol) {
                    SymbolTable.error(variabileDefinition.context,
                            variabileDefinition.name,
                            String.format("Class %s redefines inherited attribute %s", variabileDefinition.getScope().getName(), variabileDefinition.name.getText()),
                            variabileDefinition);
                    return null;
                }

                var variableType = variabileDefinition.getScope().lookup(variabileDefinition.type.getText());
                if (!(variableType instanceof TypeSymbol)) {
                    SymbolTable.error(variabileDefinition.context,
                                      variabileDefinition.type,
                                      String.format("Class %s has attribute %s with undefined type %s",
                                                    variabileDefinition.getScope().getName(),
                                                    variabileDefinition.name.getText(),
                                                    variabileDefinition.type.getText()),
                                      variabileDefinition);
                    return null;
                }
                variabileDefinition.getSymbol().setType((TypeSymbol) variableType);

                if (variabileDefinition.variableValue != null) {
                    var resolutionResult = variabileDefinition.variableValue.accept(this);
                    var valueType = (resolutionResult != null) ? resolutionResult.typeSymbol : null;
                    if (valueType != null && !((TypeSymbol) variableType).compareType(valueType)) {
                        SymbolTable.error(variabileDefinition.context,
                                resolutionResult.additionalInfo,
                                String.format("Type %s of initialization expression of attribute %s is incompatible with declared type %s",
                                              valueType.getName(),
                                              variabileDefinition.name.getText(),
                                              variabileDefinition.type.getText()),
                                variabileDefinition);
                    }

                    return null;
                }

                return new ResolutionResult((TypeSymbol) variableType, variabileDefinition.name);
            }
        }

        return null;
    }

    @Override
    public ResolutionResult visit(FunctionDefinition functionDefinition) {
        if (!functionDefinition.hasSemanticError()) {
            var classScope = (ClassSymbol) functionDefinition.getScope().getParent();
            var returnTypeDefinedSymbol = classScope.getParent().lookup(functionDefinition.type.getText());
            if (!(returnTypeDefinedSymbol instanceof TypeSymbol)) {
                SymbolTable.error(functionDefinition.context,
                        functionDefinition.type,
                        String.format("Class %s has method %s with undefined return type %s",
                                classScope.getName(),
                                functionDefinition.name.getText(),
                                functionDefinition.type.getText()),
                        functionDefinition);
                return null;
            }
            functionDefinition.getScope().setType((TypeSymbol) returnTypeDefinedSymbol);

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

                    if (!overloadedParam.getType().getName().equals(currentParam.type.getText())) {
                        SymbolTable.error(functionDefinition.context,
                                          currentParam.type,
                                          String.format("Class %s overrides method %s but changes type of formal parameter %s from %s to %s",
                                                        classScope.getName(),
                                                        functionDefinition.name.getText(),
                                                        currentParam.name.getText(),
                                                        overloadedParam.getType().getName(),
                                                        currentParam.type.getText()),
                                          functionDefinition);
                        return null;
                    }
                }

                var overloadedMethodType = ((FunctionSymbol) overloadedMethodSymbol).getType().getName();
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
                if (functionValueType != null && !((TypeSymbol) returnTypeDefinedSymbol).compareType(functionValueType)) {
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

        return null;
    }

    @Override
    public ResolutionResult visit(Int intVal) {
        return new ResolutionResult(TypeSymbol.INT, intVal.name);
    }

    @Override
    public ResolutionResult visit(StringNode string) {
        return new ResolutionResult(TypeSymbol.STRING, string.name);
    }

    @Override
    public ResolutionResult visit(BoolNode bool) {
        return new ResolutionResult(TypeSymbol.BOOL, bool.name);
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
            if (rightType != null && !rightType.getName().equals(TypeSymbol.INT.getName())) {
                SymbolTable.error(binaryOperator.context, rightResolutionResult.additionalInfo, String.format("Operand of %s has type %s instead of Int", binaryOperator.operator.getText(), rightType.getName()), binaryOperator);
                return null;
            }

            if (leftType != null && !leftType.getName().equals(TypeSymbol.INT.getName())) {
                SymbolTable.error(binaryOperator.context, leftResolutionResult.additionalInfo, String.format("Operand of %s has type %s instead of Int", binaryOperator.operator.getText(), leftType.getName()), binaryOperator);
                return null;
            }

            if (relationalOperands.contains(binaryOperator.operator.getText())) {
                return new ResolutionResult(TypeSymbol.BOOL, binaryOperator.context.start);
            } else {
                return new ResolutionResult(TypeSymbol.INT, binaryOperator.context.start);
            }
        }

        var permittedEqualityTypes = List.of("Int", "String", "Bool");
        if (rightType != null && leftType != null) {
            if (binaryOperator.operator.getText().equals("=") && permittedEqualityTypes.contains(rightType.getName()) && !rightType.getName().equals(leftType.getName())) {
                SymbolTable.error(binaryOperator.context, binaryOperator.operator, String.format("Cannot compare %s with %s", leftType.getName(), rightType.getName()), binaryOperator);
                return null;
            } else if (binaryOperator.operator.getText().equals("=") && rightType.getName().equals(leftType.getName())) {
                return new ResolutionResult(TypeSymbol.BOOL, leftResolutionResult.additionalInfo);
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
            && exprType != null && !exprType.getName().equals(TypeSymbol.INT.getName())) {
            SymbolTable.error(unaryOperator.context, exprResolutionResult.additionalInfo, String.format("Operand of %s has type %s instead of Int", unaryOperator.operator.getText(), exprType.getName()), unaryOperator);
            return null;
        }

        if (unaryOperator.operator.getText().equals("not")
            && exprType != null && !exprType.getName().equals(TypeSymbol.BOOL.getName())) {
            SymbolTable.error(unaryOperator.context, exprResolutionResult.additionalInfo, String.format("Operand of not has type %s instead of Bool", exprType.getName()), unaryOperator);
            return null;
        }

        if (unaryOperator.operator.getText().equals("isvoid")) {
            return new ResolutionResult(TypeSymbol.BOOL, (exprResolutionResult != null) ? exprResolutionResult.additionalInfo : unaryOperator.context.start);
        }

        return new ResolutionResult(exprType, (exprResolutionResult != null) ? exprResolutionResult.additionalInfo : unaryOperator.context.start);
    }

    @Override
    public ResolutionResult visit(New newNode) {
        var definedType = newNode.getScope().lookup(newNode.type.getText());
        if (!(definedType instanceof TypeSymbol)) {
            SymbolTable.error(newNode.context, newNode.type, String.format("new is used with undefined type %s", newNode.type.getText()), newNode);
            return null;
        }

        return new ResolutionResult((TypeSymbol) definedType, newNode.context.start);
    }

    @Override
    public ResolutionResult visit(Assign assign) {
        if (!assign.hasSemanticError()) {
            var definedVar = (IdSymbol)assign.getScope().lookup(assign.name.getText());
            var definedType = (definedVar != null) ? definedVar.getType() : null;
            var resolutionResult = assign.expr.accept(this);
            var exprType = (resolutionResult != null) ? resolutionResult.typeSymbol : null;

            if (definedType != null && exprType != null && !definedType.compareType(exprType)) {
                SymbolTable.error(assign.context, resolutionResult.additionalInfo, String.format("Type %s of assigned expression is incompatible with declared type %s of identifier %s", exprType.getName(), definedType.getName(), assign.name.getText()), assign);
                return null;
            }

            return new ResolutionResult(exprType, assign.context.start);
        }
        return null;
    }

    @Override
    public ResolutionResult visit(InitCall initCall) {
        return null;
    }

    @Override
    public ResolutionResult visit(Call call) {
        return null;
    }

    @Override
    public ResolutionResult visit(If ifNode) {
        var conditionResolutionResult = ifNode.cond.accept(this);
        var conditionType = (conditionResolutionResult != null) ? conditionResolutionResult.typeSymbol : null;
        if (conditionType != null && !TypeSymbol.BOOL.compareType(conditionType)) {
            SymbolTable.error(ifNode.context, conditionResolutionResult.additionalInfo, String.format("If condition has type %s instead of Bool", conditionType.getName()), ifNode);
            return new ResolutionResult(TypeSymbol.OBJECT, ifNode.context.start);
        }

        var thenResolutionResult = ifNode.thenBranch.accept(this);
        var thenType = (thenResolutionResult != null) ? thenResolutionResult.typeSymbol : null;

        var elseResolutionResult =ifNode.elseBranch.accept(this);
        var elseType = (elseResolutionResult != null) ? elseResolutionResult.typeSymbol : null;

        if (thenType != null && elseType != null) {
            return new ResolutionResult(TypeSymbol.LCA(thenType, elseType), ifNode.context.start);
        }

        return null;
    }

    @Override
    public ResolutionResult visit(While whileNode) {
        var conditionResolutionResult = whileNode.cond.accept(this);
        var conditionType = (conditionResolutionResult != null) ? conditionResolutionResult.typeSymbol : null;
        if (conditionType != null && !TypeSymbol.BOOL.compareType(conditionType)) {
            SymbolTable.error(whileNode.context, conditionResolutionResult.additionalInfo, String.format("While condition has type %s instead of Bool", conditionType.getName()), whileNode);
            return new ResolutionResult(TypeSymbol.OBJECT, whileNode.context.start);
        }

        whileNode.action.accept(this);
        return new ResolutionResult(TypeSymbol.OBJECT, whileNode.context.start);
    }

    @Override
    public ResolutionResult visit(LocalParam localParam) {
        if (!localParam.hasSemanticError()) {
            var definedType = localParam.getScope().lookup(localParam.type.getText());
            if (!(definedType instanceof TypeSymbol)) {
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

            if (localParam.value != null) {
                var resolutionResult = localParam.value.accept(this);
                var valueType = (resolutionResult != null) ? resolutionResult.typeSymbol : null;
                if (valueType != null && !((TypeSymbol) definedType).compareType(valueType)) {
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

                localParam.getSymbol().setType(valueType);
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
            var definedCaseType = caseBranch.getScope().lookup(caseBranch.type.getText());
            if (!(definedCaseType instanceof TypeSymbol)) {
                SymbolTable.error(caseBranch.context, caseBranch.type, String.format("Case variable %s has undefined type %s", caseBranch.name.getText(), caseBranch.type.getText()), caseBranch);
                return null;
            }

            var exprResolution = caseBranch.value.accept(this);
            var exprType = (exprResolution != null) ? exprResolution.typeSymbol : null;
            return new ResolutionResult(exprType, caseBranch.context.start);
        }

        return null;
    }

    @Override
    public ResolutionResult visit(Case caseNode) {
        caseNode.caseExpr.accept(this);
        TypeSymbol typeSymbol = null;
        for (var branch : caseNode.caseBranches) {
            var branchResolution = branch.accept(this);
            var branchType = (branchResolution != null) ? branchResolution.typeSymbol : null;
            if (branchType != null) {
                if (typeSymbol == null) {
                    typeSymbol = branchType;
                } else {
                    typeSymbol = TypeSymbol.LCA(typeSymbol, branchType);
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
        TypeSymbol blockType = null;
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
