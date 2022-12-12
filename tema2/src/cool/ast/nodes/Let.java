package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class Let extends Expression {
    public List<LocalParam> params;
    public Expression action;

    public Let(List<LocalParam> params, Expression action, ParserRuleContext context) {
        super(context);
        this.params = params;
        this.action = action;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
