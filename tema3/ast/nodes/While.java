package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.Scope;
import org.antlr.v4.runtime.ParserRuleContext;

public class While extends Expression {
    public Expression cond;
    public Expression action;

    public Scope scope = null;

    public While(Expression cond, Expression action, ParserRuleContext context) {
        super(context);
        this.cond = cond;
        this.action = action;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
