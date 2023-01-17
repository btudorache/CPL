package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.Scope;
import org.antlr.v4.runtime.ParserRuleContext;

public class If extends Expression {
    public Expression cond;
    public Expression thenBranch;
    public Expression elseBranch;

    public Scope scope;

    public If(Expression cond, Expression thenBranch, Expression elseBranch, ParserRuleContext context) {
        super(context);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
