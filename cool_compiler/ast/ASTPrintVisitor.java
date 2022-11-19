package cool.ast;

public class ASTPrintVisitor<T> implements ASTVisitor<T> {
    int indent = 0;

    void printIndent(String str) {
        for (int i = 0; i < indent; i++)
            System.out.print("  ");
        System.out.println(str);
    }

    @Override
    public T visit(Id id) {
        printIndent(id.token.getText());
        return null;
    }

    @Override
    public T visit(FormalParam param) {
        printIndent("formal");
        indent++;
        printIndent(param.type.getText());
        printIndent(param.name.getText());
        indent--;
        return null;
    }

    @Override
    public T visit(Program program) {
        printIndent("program");
        indent++;
        program.classes.forEach(classNode -> classNode.accept(this));
        indent--;
        return null;
    }

    @Override
    public T visit(Class myClass) {
        printIndent("class");
        indent++;
        printIndent(myClass.name.getText());
        if (myClass.inherits != null) {
            printIndent(myClass.inherits.getText());
        }
        if (myClass.features != null) {
            myClass.features.forEach(feat -> feat.accept(this));
        }
        indent--;
        return null;
    }

    @Override
    public T visit(VariabileDefinition variabileDefinition) {
        printIndent("attribute");
        indent++;
        printIndent(variabileDefinition.name.getText());
        printIndent(variabileDefinition.type.getText());
        if (variabileDefinition.variableValue != null) {
            variabileDefinition.variableValue.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public T visit(FunctionDefinition functionDefinition) {
        printIndent("method");
        indent++;
        printIndent(functionDefinition.name.getText());
        if (functionDefinition.formalParams != null) {
            functionDefinition.formalParams.forEach(param -> param.accept(this));
        }
        printIndent(functionDefinition.type.getText());
        if (functionDefinition.functionValue != null) {
            functionDefinition.functionValue.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public T visit(Int intVal) {
        printIndent(intVal.token.getText());
        return null;
    }

    @Override
    public T visit(StringNode string) {
        printIndent(string.token.getText());
        return null;
    }

    @Override
    public T visit(BoolNode bool) {
        printIndent(bool.token.getText());
        return null;
    }

    @Override
    public T visit(BinaryOperator binaryOperator) {
        printIndent(binaryOperator.operator.getText());
        indent++;
        binaryOperator.left.accept(this);
        binaryOperator.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public T visit(UnaryOperator unaryOperator) {
        printIndent(unaryOperator.operator.getText());
        indent++;
        unaryOperator.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public T visit(New newNode) {
        printIndent("new");
        indent++;
        printIndent(newNode.type.getText());
        indent--;
        return null;
    }

    @Override
    public T visit(Assign assign) {
        printIndent("<-");
        indent++;
        printIndent(assign.name.getText());
        assign.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public T visit(InitCall initCall) {
        printIndent("implicit dispatch");
        indent++;
        printIndent(initCall.name.getText());
        if (initCall.args != null) {
            initCall.args.forEach(arg -> arg.accept(this));
        }
        indent--;
        return null;
    }

    @Override
    public T visit(Call call) {
        printIndent(".");
        indent++;
        call.prefix.accept(this);
        if (call.atType != null) {
            printIndent(call.atType.getText());
        }
        printIndent(call.name.getText());
        if (call.args != null) {
            call.args.forEach(arg -> arg.accept(this));
        }
        indent--;
        return null;
    }

    @Override
    public T visit(If ifNode) {
        printIndent("if");
        indent++;
        ifNode.cond.accept(this);
        ifNode.thenBranch.accept(this);
        ifNode.elseBranch.accept(this);
        indent--;
        return null;
    }

    @Override
    public T visit(While whileNode) {
        printIndent("while");
        indent++;
        whileNode.cond.accept(this);
        whileNode.action.accept(this);
        indent--;
        return null;
    }

    @Override
    public T visit(LocalParam localParam) {
        printIndent("local");
        indent++;
        printIndent(localParam.name.getText());
        printIndent(localParam.type.getText());
        if (localParam.value != null) {
            localParam.value.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public T visit(Let let) {
        printIndent("let");
        indent++;
        let.params.forEach(param -> param.accept(this));
        let.action.accept(this);
        indent--;
        return null;
    }

    @Override
    public T visit(CaseBranch caseBranch) {
        printIndent("case branch");
        indent++;
        printIndent(caseBranch.name.getText());
        printIndent(caseBranch.type.getText());
        caseBranch.value.accept(this);
        indent--;
        return null;
    }

    @Override
    public T visit(Case caseNode) {
        printIndent("case");
        indent++;
        caseNode.caseExpr.accept(this);
        caseNode.caseBranches.forEach(branch -> branch.accept(this));
        indent--;
        return null;
    }

    @Override
    public T visit(Block block) {
        printIndent("block");
        indent++;
        block.actions.forEach(action -> action.accept(this));
        indent--;
        return null;
    }
}
