package cool.ast;

public interface ASTVisitor<T> {

    T visit(Id id);

    T visit(FormalParam param);

    T visit(Program program);

    T visit(Class myClass);

    T visit(VariabileDefinition variabileDefinition);

    T visit(FunctionDefinition functionDefinition);

    T visit(Int intVal);

    T visit(StringNode string);

    T visit(BoolNode bool);

    T visit(BinaryOperator binaryOperator);

    T visit(UnaryOperator unaryOperator);

    T visit(New newNode);

    T visit(Assign assign);

    T visit(InitCall initCall);

    T visit(Call call);

    T visit(If ifNode);

    T visit(While whileNode);

    T visit(LocalParam localParam);

    T visit(Let let);

    T visit(CaseBranch caseBranch);

    T visit(Case caseNode);

    T visit(Block block);
}

