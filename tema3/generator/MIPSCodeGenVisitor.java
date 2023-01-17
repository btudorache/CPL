package cool.generator;

import cool.ast.ASTVisitor;
import cool.ast.nodes.Class;
import cool.ast.nodes.*;
import cool.compiler.Compiler;
import cool.parser.CoolParser;
import cool.semantics.structures.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MIPSCodeGenVisitor implements ASTVisitor<CodeGenResult> {
    static STGroupFile templates = new STGroupFile("cool/generator/cgen.stg");
    static String STRING_LABEL_COUNTER = "str_const";
    static String INT_LABEL_COUNTER = "int_const";
    static String DISPATCH_LABEL_COUNTER = "dispatch";
    static String IF_LABEL_COUNTER = "if_label";
    static String VOID_LABEL_COUNTER = "void_label";
    static String NOT_LABEL_COUNTER = "not_label";
    static String EQUALITY_LABEL_COUNTER = "equality_label";
    static String COMPARE_LABEL_COUNTER = "compare_label";
    static String WHILE_LABEL_COUNTER = "while_label";
    static String CASE_LABEL_COUNTER = "case_label";
    static String CASE_BRANCH_LABEL_COUNTER = "case_branch_label";

    public static int labelCount = 0;

    ST strings;
    ST ints;
    ST classNames;
    ST classInitAndDispatch;
    ST classPrototypes;
    ST classDispatchTables;
    ST funcs;

    Map<String, Integer> labelCounter;
    Map<String, Integer> declaredStrings;
    Map<Integer, Integer> declaredInts;

    public void assignLabels(List<ClassSymbol> classes) {
        Map<String, List<ClassSymbol>> neighbours = new HashMap<>();
        var initialClasses = new ArrayList<ClassSymbol>();
        initialClasses.add(ClassSymbol.INT);
        initialClasses.add(ClassSymbol.STRING);
        initialClasses.add(ClassSymbol.BOOL);
        initialClasses.add(ClassSymbol.IO);
        neighbours.put("Object", initialClasses);

        for (var classElem : classes) {
            if (!neighbours.containsKey(classElem.inheritedScope.name)) {
                var classNeigh = new ArrayList<ClassSymbol>();
                classNeigh.add(classElem);
                neighbours.put(classElem.inheritedScope.name, classNeigh);
            } else {
                var classElemNeighbours = neighbours.get(classElem.inheritedScope.name);
                classElemNeighbours.add(classElem);
            }
        }

        assignLabeslRec(ClassSymbol.OBJECT, neighbours);
    }

    private void assignLabeslRec(ClassSymbol classElem, Map<String, List<ClassSymbol>> neighbours) {
        classElem.label = labelCount;

        var className = classElem.name;
        if (!declaredStrings.containsKey(className)) {
            addStringLiteral(className);
        }

        var classNameST = templates.getInstanceOf("word");
        classNameST.add("value", String.format(STRING_LABEL_COUNTER + declaredStrings.get(className)));
        classNames.add("e", classNameST);

        var classProtoObj = templates.getInstanceOf("word");
        classProtoObj.add("value", className + "_protObj");
        classInitAndDispatch.add("e", classProtoObj);

        var classInit = templates.getInstanceOf("word");
        classInit.add("value", className + "_init");
        classInitAndDispatch.add("e", classInit);

        var classPrototypeST = templates.getInstanceOf("classPrototype");
        classPrototypeST.add("className", className);
        classPrototypeST.add("classLabel", classElem.label);
        var classVariables = classElem.getClassVariables();
        for (var variableType : classVariables) {
            var variableDeclarationWord = templates.getInstanceOf("word");
            if (variableType.equals(ClassSymbol.INT)) {
                variableDeclarationWord.add("value", "int_const0");
            } else if (variableType.equals(ClassSymbol.STRING)) {
                variableDeclarationWord.add("value", "str_const0");
            } else if (variableType.equals(ClassSymbol.BOOL)) {
                variableDeclarationWord.add("value", "bool_const0");
            } else {
                variableDeclarationWord.add("value", 0);
            }
            classPrototypeST.add("variables", variableDeclarationWord);
        }

        int prototypeHeaderSize = 3;
        if (className.equals("Int") || className.equals("Bool")) {
            prototypeHeaderSize += 1;
            classPrototypeST.add("variables", "    .word   0");
        } else if (className.equals("String")) {
            prototypeHeaderSize += 2;
            classPrototypeST.add("variables", "    .word   int_const0");
            classPrototypeST.add("variables", "    .asciiz \"\"");
            classPrototypeST.add("variables", "    .align  2");
        }

        classPrototypeST.add("size", prototypeHeaderSize + classVariables.size());
        classPrototypes.add("e", classPrototypeST);

        var classDispatchTableST = templates.getInstanceOf("classDispatchTable");
        classDispatchTableST.add("className", className);
        for (var method : classElem.getClassMethods()) {
            var methodWordST = templates.getInstanceOf("word");
            methodWordST.add("value", method);
            classDispatchTableST.add("methods", methodWordST);
        }
        classDispatchTables.add("e", classDispatchTableST);

        if (neighbours.containsKey(classElem.name)) {
            for (var neigh : neighbours.get(classElem.name)) {
                labelCount++;
                assignLabeslRec(neigh, neighbours);
            }
        }

        classElem.lowerClassBound = labelCount;
    }

    private int addIntLiteral(int value) {
        declaredInts.put(value, labelCounter.get(INT_LABEL_COUNTER));

        var intST = templates.getInstanceOf("int");
        intST.add("labelCount", labelCounter.get(INT_LABEL_COUNTER));
        intST.add("value", value);

        labelCounter.put(INT_LABEL_COUNTER, labelCounter.get(INT_LABEL_COUNTER) + 1);
        ints.add("e", intST);

        return labelCounter.get(INT_LABEL_COUNTER) - 1;
    }

    private int addStringLiteral(String stringValue) {
        var stringST = templates.getInstanceOf("string");
        stringST.add("value", stringValue);

        declaredStrings.put(stringValue, labelCounter.get(STRING_LABEL_COUNTER));
        stringST.add("labelCount", labelCounter.get(STRING_LABEL_COUNTER));
        labelCounter.put(STRING_LABEL_COUNTER, labelCounter.get(STRING_LABEL_COUNTER) + 1);

        var stringSize = stringValue.length();
        int sizeLabel;
        if (declaredInts.containsKey(stringSize)) {
            sizeLabel = declaredInts.get(stringSize);
        } else {
            sizeLabel = addIntLiteral(stringSize);
        }
        stringST.add("sizeLabel", sizeLabel);

        var stringStructSize = (stringSize + 1) / 4 + 4;
        if ((stringSize + 1) % 4 != 0) {
            stringStructSize += 1;
        }
        stringST.add("size", stringStructSize);

        strings.add("e", stringST);

        return labelCounter.get(STRING_LABEL_COUNTER) - 1;
    }

    private String getFileName(ParserRuleContext context) {
        while (!(context.getParent() instanceof CoolParser.ProgramContext)) {
            context = context.getParent();
        }

        return new File(Compiler.fileNames.get(context)).getName();
    }

    @Override
    public CodeGenResult visit(Id id) {
        if (id.name.getText().equals("self")) {
            return new CodeGenResult(new ST("    move    $a0 $s0"), id.scope.lookupClass());
        }

        var idSymbol = (IdSymbol) id.scope.lookup(id.name.getText());
        if (idSymbol == null) {
            idSymbol = (IdSymbol) id.scope.lookupClass().lookupInheritanceTree(id.name.getText());
        }

        if (idSymbol.idType.equals(IdSymbolType.ClassIdSymbol)) {
            var classIdSymbolST = new ST("    lw      $a0 <offset>($s0)");
            classIdSymbolST.add("offset", idSymbol.offset);
            return new CodeGenResult(classIdSymbolST, idSymbol.type.equals(ClassSymbol.SELF_TYPE) ? id.scope.lookupClass() : idSymbol.type);
        } else if (idSymbol.idType.equals(IdSymbolType.FormalIdSymbol) || idSymbol.idType.equals(IdSymbolType.LocalIdSymbol)) {
            var formalIdSymbolST = new ST("    lw      $a0 <offset>($fp)");
            formalIdSymbolST.add("offset", idSymbol.offset);
            return new CodeGenResult(formalIdSymbolST, idSymbol.type.equals(ClassSymbol.SELF_TYPE) ? id.scope.lookupClass() : idSymbol.type);
        }

        return null;
    }

    @Override
    public CodeGenResult visit(FormalParam param) {
        return null;
    }

    @Override
    public CodeGenResult visit(Program program) {
        labelCounter = new HashMap<>();
        labelCounter.put(STRING_LABEL_COUNTER, 6);
        labelCounter.put(INT_LABEL_COUNTER, 5);
        labelCounter.put(DISPATCH_LABEL_COUNTER, 0);
        labelCounter.put(IF_LABEL_COUNTER, 0);
        labelCounter.put(VOID_LABEL_COUNTER, 0);
        labelCounter.put(NOT_LABEL_COUNTER, 0);
        labelCounter.put(EQUALITY_LABEL_COUNTER, 0);
        labelCounter.put(COMPARE_LABEL_COUNTER, 0);
        labelCounter.put(WHILE_LABEL_COUNTER, 0);
        labelCounter.put(CASE_LABEL_COUNTER, 0);
        labelCounter.put(CASE_BRANCH_LABEL_COUNTER, 0);

        declaredStrings = new HashMap<>();
        declaredStrings.put("", 0);

        declaredInts = new HashMap<>();
        declaredInts.put(0, 0);

        strings = templates.getInstanceOf("sequence");
        ints = templates.getInstanceOf("sequence");
        classNames = templates.getInstanceOf("sequence");

        classInitAndDispatch = templates.getInstanceOf("sequence");
        classPrototypes = templates.getInstanceOf("sequence");
        classDispatchTables = templates.getInstanceOf("sequence");
        funcs = templates.getInstanceOf("sequence");

        assignLabels(program.classes.stream().map(classNode -> classNode.scope).collect(Collectors.toList()));
        program.classes.forEach(classNode -> classNode.accept(this));

        var programST = templates.getInstanceOf("program");
        programST.add("strings", strings);
        programST.add("ints", ints);
        programST.add("classNames", classNames);
        programST.add("classInitAndDispatch", classInitAndDispatch);
        programST.add("classPrototypes", classPrototypes);
        programST.add("classDispatchTables", classDispatchTables);
        programST.add("funcs", funcs);

        return new CodeGenResult(programST);
    }

    @Override
    public CodeGenResult visit(Class myClass) {
        var className = myClass.name.getText();

        // add object constructor method
        var classInitST = templates.getInstanceOf("classInit");
        classInitST.add("className", className);
        classInitST.add("inheritedClass", myClass.scope.getInheritedScope().getName());

        for (var feature : myClass.features) {
            if (feature instanceof VariableDefinition) {
                var variableDefGenResult = feature.accept(this);
                if (variableDefGenResult != null) {
                    classInitST.add("body", variableDefGenResult.template);
                }
            }
        }
        funcs.add("e", classInitST);

        for (var feature: myClass.features) {
            if (feature instanceof FunctionDefinition) {
                feature.accept(this);
            }
        }

        return null;
    }

    @Override
    public CodeGenResult visit(VariableDefinition variabileDefinition) {
        if (variabileDefinition.variableValue != null) {
            var variableDefinitionST = templates.getInstanceOf("variableInit");
            var codeGenResult = variabileDefinition.variableValue.accept(this);
            variableDefinitionST.add("initResult", codeGenResult.template);
            variableDefinitionST.add("variableOffset", variabileDefinition.symbol.offset);
            variableDefinitionST.add("variableTypeRegister", "$s0");
            return new CodeGenResult(variableDefinitionST, codeGenResult.staticType);
        }

        return null;
    }

    @Override
    public CodeGenResult visit(FunctionDefinition functionDefinition) {
        var funcDefST = templates.getInstanceOf("funcDef");
        funcDefST.add("name", String.format("%s.%s", functionDefinition.scope.parent.getName(), functionDefinition.scope.getName()));
        if (functionDefinition.formalParams.size() > 0) {
            funcDefST.add("numParams", functionDefinition.formalParams.size() * 4);
        }
        funcDefST.add("body", functionDefinition.functionValue.accept(this).template);

        funcs.add("e", funcDefST);

        return null;
    }

    @Override
    public CodeGenResult visit(Int intVal) {
        var intValue = Integer.parseInt(intVal.name.getText());
        int label;
        if (declaredInts.containsKey(intValue)) {
            label = declaredInts.get(intValue);
        } else {
            label = addIntLiteral(intValue);
        }

        var literalSt = templates.getInstanceOf("literal");
        literalSt.add("label", String.format("int_const%d", label));

        return new CodeGenResult(literalSt, ClassSymbol.INT);
    }

    @Override
    public CodeGenResult visit(StringNode string) {
        var stringValue = string.name.getText();
        if (stringValue.contains("\n")) {
            stringValue = stringValue.replaceAll("\n", "\\\\n");
        }

        int label;
        if (declaredStrings.containsKey(stringValue)) {
            label = declaredStrings.get(stringValue);
        } else {
            label = addStringLiteral(stringValue);
        }

        var literalST = templates.getInstanceOf("literal");
        literalST.add("label", String.format("str_const%d", label));

        return new CodeGenResult(literalST, ClassSymbol.STRING);
    }

    @Override
    public CodeGenResult visit(BoolNode bool) {
        var boolValue = bool.name.getText();
        var literalSt = templates.getInstanceOf("literal");
        if (boolValue.equals("true")) {
            literalSt.add("label", "bool_const1");
        } else {
            literalSt.add("label", "bool_const0");
        }

        return new CodeGenResult(literalSt, ClassSymbol.BOOL);
    }

    @Override
    public CodeGenResult visit(BinaryOperator binaryOperator) {
        var binaryArithmeticOperators = List.of("+", "-", "*", "/");
        var binaryEqualityOperators = List.of("<=", "<");
        var operator = binaryOperator.operator.getText();
        if (binaryArithmeticOperators.contains(operator)) {
            var binaryArithmeticST = templates.getInstanceOf("binaryArithmeticOperator");
            binaryArithmeticST.add("expr1", binaryOperator.left.accept(this).template);
            binaryArithmeticST.add("expr2", binaryOperator.right.accept(this).template);

            switch (operator) {
                case "+" -> binaryArithmeticST.add("op", "add");
                case "-" -> binaryArithmeticST.add("op", "sub");
                case "*" -> binaryArithmeticST.add("op", "mul");
                case "/" -> binaryArithmeticST.add("op", "div");
            }

            return new CodeGenResult(binaryArithmeticST, ClassSymbol.INT);
        } else if (operator.equals("=")) {
            var equalityST = templates.getInstanceOf("equality");
            equalityST.add("expr1", binaryOperator.left.accept(this).template);
            equalityST.add("expr2", binaryOperator.right.accept(this).template);
            equalityST.add("label", labelCounter.get(EQUALITY_LABEL_COUNTER));
            labelCounter.put(EQUALITY_LABEL_COUNTER, labelCounter.get(EQUALITY_LABEL_COUNTER) + 1);

            return new CodeGenResult(equalityST, ClassSymbol.BOOL);
        } else if (binaryEqualityOperators.contains(operator)) {
            var compareST = templates.getInstanceOf("compare");
            compareST.add("expr1", binaryOperator.left.accept(this).template);
            compareST.add("expr2", binaryOperator.right.accept(this).template);
            switch (operator) {
                case "<=" -> compareST.add("op", "ble");
                case "<" -> compareST.add("op", "blt");
            }
            compareST.add("label", labelCounter.get(COMPARE_LABEL_COUNTER));
            labelCounter.put(COMPARE_LABEL_COUNTER, labelCounter.get(COMPARE_LABEL_COUNTER) + 1);

            return new CodeGenResult(compareST, ClassSymbol.BOOL);
        }

        return null;
    }

    @Override
    public CodeGenResult visit(UnaryOperator unaryOperator) {
        switch (unaryOperator.operator.getText()) {
            case "isvoid" -> {
                var isVoidST = templates.getInstanceOf("isVoid");
                isVoidST.add("body", unaryOperator.expr.accept(this).template);
                isVoidST.add("voidLabel", labelCounter.get(VOID_LABEL_COUNTER));
                labelCounter.put(VOID_LABEL_COUNTER, labelCounter.get(VOID_LABEL_COUNTER) + 1);
                return new CodeGenResult(isVoidST, ClassSymbol.BOOL);
            }
            case "not" -> {
                var notST = templates.getInstanceOf("not");
                notST.add("body", unaryOperator.expr.accept(this).template);
                notST.add("notLabel", labelCounter.get(NOT_LABEL_COUNTER));
                labelCounter.put(NOT_LABEL_COUNTER, labelCounter.get(NOT_LABEL_COUNTER) + 1);
                return new CodeGenResult(notST, ClassSymbol.BOOL);
            }
            case "~" -> {
                var complementST = templates.getInstanceOf("complement");
                complementST.add("expr", unaryOperator.expr.accept(this).template);
                return new CodeGenResult(complementST, ClassSymbol.INT);
            }
        }

        return null;
    }

    @Override
    public CodeGenResult visit(New newNode) {
        if (newNode.type.getText().equals("SELF_TYPE")) {
            return new CodeGenResult(templates.getInstanceOf("newObjectSelfType"), ClassSymbol.SELF_TYPE);
        } else {
            var newObjectStaticST = templates.getInstanceOf("newObjectStatic");
            newObjectStaticST.add("className", newNode.type.getText());
            return new CodeGenResult(newObjectStaticST, (ClassSymbol) newNode.scope.lookup(newNode.type.getText()));
        }
    }

    @Override
    public CodeGenResult visit(Assign assign) {
        var variableInitST = templates.getInstanceOf("variableInit");

        var assignCodeGenRes = assign.expr.accept(this);
        var idSymbol = (IdSymbol) assign.scope.lookup(assign.name.getText());
        if (idSymbol == null) {
            idSymbol = (IdSymbol) assign.scope.lookupClass().lookupInheritanceTree(assign.name.getText());
        }

        variableInitST.add("initResult", assignCodeGenRes.template);
        if (idSymbol.idType.equals(IdSymbolType.ClassIdSymbol)) {
            variableInitST.add("variableTypeRegister", "$s0");
        } else if (idSymbol.idType.equals(IdSymbolType.FormalIdSymbol) || idSymbol.idType.equals(IdSymbolType.LocalIdSymbol)) {
            variableInitST.add("variableTypeRegister", "$fp");
        }
        variableInitST.add("variableOffset", idSymbol.offset);

        return new CodeGenResult(variableInitST, idSymbol.type);
    }

    @Override
    public CodeGenResult visit(ImplicitCall implicitCall) {
        var dispatchST = templates.getInstanceOf("dispatch");
        dispatchST.add("implicitDispatch", "true");

        var scope = implicitCall.scope.lookupClass();
        var method = (FunctionSymbol) scope.lookup(implicitCall.name.getText());
        if (method == null) {
            method = (FunctionSymbol) scope.lookupInheritanceTree(implicitCall.name.getText());
        }

        // TODO: add method args
        for (int i = implicitCall.args.size() - 1; i >= 0; i--) {
            var callArg = templates.getInstanceOf("dispatchArg");
            callArg.add("expr", implicitCall.args.get(i).accept(this).template);
            dispatchST.add("args", callArg);
        }


        dispatchST.add("fileLine", implicitCall.context.start.getLine());
        var fileName = getFileName(implicitCall.context);
        if (!declaredStrings.containsKey(fileName)) {
            addStringLiteral(fileName);
        }
        dispatchST.add("fileNameLabel", declaredStrings.get(fileName));
        dispatchST.add("methodOffset", method.dispatchTableOffset);
        dispatchST.add("dispatchLabel", labelCounter.get(DISPATCH_LABEL_COUNTER));
        labelCounter.put(DISPATCH_LABEL_COUNTER, labelCounter.get(DISPATCH_LABEL_COUNTER) + 1);

        return new CodeGenResult(dispatchST, method.type.equals(ClassSymbol.SELF_TYPE) ? implicitCall.scope.lookupClass() : method.type);
    }

    @Override
    public CodeGenResult visit(Call call) {
        var dispatchST = templates.getInstanceOf("dispatch");
        var dispatcherResult = call.prefix.accept(this);
        var method = (FunctionSymbol) dispatcherResult.staticType.lookup(call.name.getText());
        if (method == null) {
            method = (FunctionSymbol) dispatcherResult.staticType.lookupInheritanceTree(call.name.getText());
        }

        // TODO: add method args
        for (int i = call.args.size() - 1; i >= 0; i--) {
            var callArg = templates.getInstanceOf("dispatchArg");
            callArg.add("expr", call.args.get(i).accept(this).template);
            dispatchST.add("args", callArg);
        }


        dispatchST.add("fileLine", call.context.start.getLine());
        var fileName = getFileName(call.context);
        if (!declaredStrings.containsKey(fileName)) {
            addStringLiteral(fileName);
        }
        dispatchST.add("fileNameLabel", declaredStrings.get(fileName));
        dispatchST.add("dispatcherCode", dispatcherResult.template);
        dispatchST.add("methodOffset", method.dispatchTableOffset);
        dispatchST.add("dispatchLabel", labelCounter.get(DISPATCH_LABEL_COUNTER));
        labelCounter.put(DISPATCH_LABEL_COUNTER, labelCounter.get(DISPATCH_LABEL_COUNTER) + 1);

        if (call.atType != null) {
            dispatchST.add("staticDispatch", call.atType.getText() + "_dispTab");
        }

        return new CodeGenResult(dispatchST, method.type.equals(ClassSymbol.SELF_TYPE) ? call.scope.lookupClass() : method.type);
    }

    @Override
    public CodeGenResult visit(If ifNode) {
        var ifST = templates.getInstanceOf("ifExpr");

        ifST.add("condBranch", ifNode.cond.accept(this).template);
        ifST.add("firstBranch", ifNode.thenBranch.accept(this).template);
        ifST.add("secondBranch", ifNode.elseBranch.accept(this).template);
        ifST.add("ifCount", labelCounter.get(IF_LABEL_COUNTER));
        labelCounter.put(IF_LABEL_COUNTER, labelCounter.get(IF_LABEL_COUNTER) + 1);

        return new CodeGenResult(ifST);
    }

    @Override
    public CodeGenResult visit(While whileNode) {
        var whileST = templates.getInstanceOf("while");

        whileST.add("cond", whileNode.cond.accept(this).template);
        var actionCodeGenRes =  whileNode.action.accept(this);
        whileST.add("body", actionCodeGenRes.template);
        whileST.add("label", labelCounter.get(WHILE_LABEL_COUNTER));
        labelCounter.put(WHILE_LABEL_COUNTER, labelCounter.get(WHILE_LABEL_COUNTER) + 1);

        return new CodeGenResult(whileST, actionCodeGenRes.staticType);
    }

    @Override
    public CodeGenResult visit(LocalParam localParam) {
        var letParamSt = templates.getInstanceOf("letParam");
        if (localParam.value != null) {
            letParamSt.add("assignmentBody", localParam.value.accept(this).template);
        } else {
            if (localParam.symbol.type.equals(ClassSymbol.INT)) {
                letParamSt.add("assignmentBody", "    la      $a0 int_const0");
            } else if (localParam.symbol.type.equals(ClassSymbol.STRING)) {
                letParamSt.add("assignmentBody", "    la      $a0 str_const0");
            } else if (localParam.symbol.type.equals(ClassSymbol.BOOL)) {
                letParamSt.add("assignmentBody", "    la      $a0 bool_const0");
            } else {
                letParamSt.add("assignmentBody", "    la      $a0 0");
            }
        }

        letParamSt.add("offset", localParam.symbol.offset);
        return new CodeGenResult(letParamSt, localParam.symbol.type);
    }

    @Override
    public CodeGenResult visit(Let let) {
        var letST = templates.getInstanceOf("let");
        letST.add("numArgsSize", let.params.size() * 4);
        for (var param : let.params) {
            var codeGenRes = param.accept(this);
            letST.add("args", codeGenRes.template);
        }

        var actionGenRes = let.action.accept(this);
        letST.add("body", actionGenRes.template);

        return new CodeGenResult(letST, actionGenRes.staticType);
    }

    @Override
    public CodeGenResult visit(CaseBranch caseBranch) {
        var caseBranchST = templates.getInstanceOf("caseBranch");
        caseBranchST.add("expr", caseBranch.value.accept(this).template);

        caseBranchST.add("caseBranchLabel", labelCounter.get(CASE_BRANCH_LABEL_COUNTER));
        labelCounter.put(CASE_BRANCH_LABEL_COUNTER, labelCounter.get(CASE_BRANCH_LABEL_COUNTER) + 1);

        caseBranchST.add("caseLabel", labelCounter.get(CASE_LABEL_COUNTER) - 1);

        // TODO: maybe look for classSymbol in the global scope
        var varType = (ClassSymbol) caseBranch.scope.lookup(caseBranch.type.getText());
        caseBranchST.add("lowerBound", varType.label);
        caseBranchST.add("upperBound", varType.lowerClassBound);

        return new CodeGenResult(caseBranchST, varType);
    }

    @Override
    public CodeGenResult visit(Case caseNode) {
        var caseST = templates.getInstanceOf("case");
        caseST.add("expr", caseNode.caseExpr.accept(this).template);

        caseST.add("label", labelCounter.get(CASE_LABEL_COUNTER));
        labelCounter.put(CASE_LABEL_COUNTER, labelCounter.get(CASE_LABEL_COUNTER) + 1);

        caseST.add("fileLine", caseNode.context.start.getLine());

        var fileName = getFileName(caseNode.context);
        if (!declaredStrings.containsKey(fileName)) {
            addStringLiteral(fileName);
        }
        caseST.add("fileNameLabel", declaredStrings.get(fileName));

        var branchesCodeGenResults = new ArrayList<CodeGenResult>();
        for (var caseBranch : caseNode.caseBranches) {
            branchesCodeGenResults.add(caseBranch.accept(this));
        }
        branchesCodeGenResults.sort((o1, o2) -> o2.staticType.label - o1.staticType.label);

        for (var codeGenRes : branchesCodeGenResults) {
            caseST.add("branches", codeGenRes.template);
        }

        return new CodeGenResult(caseST);
    }

    @Override
    public CodeGenResult visit(Block block) {
        var sequenceST = templates.getInstanceOf("sequence");
        CodeGenResult codeGenResult = null;
        for (var action : block.actions) {
            codeGenResult = action.accept(this);
            sequenceST.add("e", codeGenResult.template);
        }

        return new CodeGenResult(sequenceST, codeGenResult.staticType);
    }
}
