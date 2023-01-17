package cool.ast;

import cool.ast.nodes.*;
import cool.ast.nodes.Class;
public class ASTPrintVisitor implements ASTVisitor<Void> {
    int indent = 0;

    void printIndent(String str) {
        for (int i = 0; i < indent; i++)
            System.out.print("  ");
        System.out.println(str);
    }

    @Override
    public Void visit(Id id) {
        printIndent(id.name.getText());
        return null;
    }

    @Override
    public Void visit(FormalParam param) {
        printIndent("formal");
        indent++;
        printIndent(param.name.getText());
        printIndent(param.type.getText());
        indent--;
        return null;
    }

    @Override
    public Void visit(Program program) {
        printIndent("program");
        indent++;
        program.classes.forEach(classNode -> classNode.accept(this));
        indent--;
        return null;
    }

    @Override
    public Void visit(Class myClass) {
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
    public Void visit(VariableDefinition variabileDefinition) {
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
    public Void visit(FunctionDefinition functionDefinition) {
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
    public Void visit(Int intVal) {
        printIndent(intVal.name.getText());
        return null;
    }

    @Override
    public Void visit(StringNode string) {
        printIndent(string.name.getText());
        return null;
    }

    @Override
    public Void visit(BoolNode bool) {
        printIndent(bool.name.getText());
        return null;
    }

    @Override
    public Void visit(BinaryOperator binaryOperator) {
        printIndent(binaryOperator.operator.getText());
        indent++;
        binaryOperator.left.accept(this);
        binaryOperator.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(UnaryOperator unaryOperator) {
        printIndent(unaryOperator.operator.getText());
        indent++;
        unaryOperator.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(New newNode) {
        printIndent("new");
        indent++;
        printIndent(newNode.type.getText());
        indent--;
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        printIndent("<-");
        indent++;
        printIndent(assign.name.getText());
        assign.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(ImplicitCall implicitCall) {
        printIndent("implicit dispatch");
        indent++;
        printIndent(implicitCall.name.getText());
        if (implicitCall.args != null) {
            implicitCall.args.forEach(arg -> arg.accept(this));
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(Call call) {
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
    public Void visit(If ifNode) {
        printIndent("if");
        indent++;
        ifNode.cond.accept(this);
        ifNode.thenBranch.accept(this);
        ifNode.elseBranch.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(While whileNode) {
        printIndent("while");
        indent++;
        whileNode.cond.accept(this);
        whileNode.action.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(LocalParam localParam) {
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
    public Void visit(Let let) {
        printIndent("let");
        indent++;
        let.params.forEach(param -> param.accept(this));
        let.action.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
        printIndent("case branch");
        indent++;
        printIndent(caseBranch.name.getText());
        printIndent(caseBranch.type.getText());
        caseBranch.value.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Case caseNode) {
        printIndent("case");
        indent++;
        caseNode.caseExpr.accept(this);
        caseNode.caseBranches.forEach(branch -> branch.accept(this));
        indent--;
        return null;
    }

    @Override
    public Void visit(Block block) {
        printIndent("block");
        indent++;
        block.actions.forEach(action -> action.accept(this));
        indent--;
        return null;
    }
}
