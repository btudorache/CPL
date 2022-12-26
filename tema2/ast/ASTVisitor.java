package cool.ast;


import cool.ast.nodes.*;
import cool.ast.nodes.Class;

public interface ASTVisitor<T> {
    T visit(Id id);

    T visit(FormalParam param);

    T visit(Program program);

    T visit(Class myClass);

    T visit(VariableDefinition variabileDefinition);

    T visit(FunctionDefinition functionDefinition);

    T visit(Int intVal);

    T visit(StringNode string);

    T visit(BoolNode bool);

    T visit(BinaryOperator binaryOperator);

    T visit(UnaryOperator unaryOperator);

    T visit(New newNode);

    T visit(Assign assign);

    T visit(ImplicitCall implicitCall);

    T visit(Call call);

    T visit(If ifNode);

    T visit(While whileNode);

    T visit(LocalParam localParam);

    T visit(Let let);

    T visit(CaseBranch caseBranch);

    T visit(Case caseNode);

    T visit(Block block);
}

