package cool.ast;

import org.antlr.v4.runtime.Token;

import java.util.List;

public abstract class ASTNode {
    Token token;

    ASTNode(Token token) {
        this.token = token;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

class Program extends ASTNode {
    List<Class> classes;

    Program(List<Class> classes, Token token) {
        super(token);
        this.classes = classes;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Class extends ASTNode {
    Token name;
    Token inherits;
    List<Feature> features;

    Class(Token name, Token inherits, List<Feature> features, Token token) {
        super(token);
        this.features = features;
        this.inherits = inherits;
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Feature extends ASTNode {
    Token name;
    Token type;

    Feature(Token name, Token type, Token token) {
        super(token);
        this.name = name;
        this.type = type;
    }
}

class New extends Expression {
    Token type;

    New(Token type, Token token) {
        super(token);
        this.type = type;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Assign extends Expression {
    Token name;
    Expression expr;

    Assign(Token name, Expression expr, Token token) {
        super(token);
        this.name = name;
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class VariabileDefinition extends Feature {
    Expression variableValue;

    VariabileDefinition(Expression variableValue, Token name, Token type, Token token) {
        super(name, type, token);
        this.variableValue = variableValue;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class If extends Expression {
    Expression cond;
    Expression thenBranch;
    Expression elseBranch;


    If(Expression cond, Expression thenBranch, Expression elseBranch, Token token) {
        super(token);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class While extends Expression {
    Expression cond;
    Expression action;


    While(Expression cond, Expression action, Token token) {
        super(token);
        this.cond = cond;
        this.action = action;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    List<LocalParam> params;
    Expression action;


    Let(List<LocalParam> params, Expression action, Token token) {
        super(token);
        this.params = params;
        this.action = action;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class LocalParam extends ASTNode {
    Token name;
    Token type;
    Expression value;


    LocalParam(Token name, Token type, Expression value, Token token) {
        super(token);
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Case extends Expression {
    Expression caseExpr;
    List<CaseBranch> caseBranches;


    Case(Expression caseExpr, List<CaseBranch> caseBranches, Token token) {
        super(token);
        this.caseExpr = caseExpr;
        this.caseBranches = caseBranches;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class CaseBranch extends ASTNode {
    Token name;
    Token type;
    Expression value;

    CaseBranch(Token name, Token type, Expression value, Token token) {
        super(token);
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class BinaryOperator extends Expression {
    Expression left;
    Expression right;
    Token operator;


    BinaryOperator(Expression left, Expression right, Token operator, Token token) {
        super(token);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Call extends Expression {
    Expression prefix;
    Token atType;
    Token name;
    List<Expression> args;

    Call(Expression prefix, Token atType, Token name, List<Expression> args, Token token) {
        super(token);
        this.prefix = prefix;
        this.atType = atType;
        this.name = name;
        this.args = args;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class UnaryOperator extends Expression {
    Expression expr;
    Token operator;

    UnaryOperator(Expression expr, Token operator, Token token) {
        super(token);
        this.expr = expr;
        this.operator = operator;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class FunctionDefinition extends Feature {
    Expression functionValue;
    List<FormalParam> formalParams;


    FunctionDefinition(Expression functionValue, List<FormalParam> formalParams, Token name, Token type, Token token) {
        super(name, type, token);
        this.functionValue = functionValue;
        this.formalParams = formalParams;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class InitCall extends Expression {
    Token name;
    List<Expression> args;

    InitCall(Token name, List<Expression> args, Token token) {
        super(token);
        this.name = name;
        this.args = args;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Expression extends ASTNode {
    Expression(Token token) {
        super(token);
    }
}

class Id extends Expression {

    Id(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Int extends Expression {
    Int(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class StringNode extends Expression {
    StringNode(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class BoolNode extends Expression {
    BoolNode(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    List<Expression> actions;

    Block(List<Expression> actions, Token token) {
        super(token);
        this.actions = actions;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class FormalParam extends ASTNode {
    Token type;
    Token name;

    FormalParam(Token type, Token name, Token token) {
        super(token);
        this.type = type;
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
