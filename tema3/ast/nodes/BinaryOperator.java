package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class BinaryOperator extends Expression {
    public Expression left;
    public Expression right;
    public Token operator;
    public BinaryOperator(Expression left, Expression right, Token operator, ParserRuleContext context) {
        super(context);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}