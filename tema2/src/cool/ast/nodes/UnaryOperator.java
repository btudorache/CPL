package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class UnaryOperator extends Expression {
    public Expression expr;
    public Token operator;

    public UnaryOperator(Expression expr, Token operator, ParserRuleContext context) {
        super(context);
        this.expr = expr;
        this.operator = operator;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
