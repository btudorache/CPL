package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class Block extends Expression {
    public List<Expression> actions;

    public Block(List<Expression> actions, ParserRuleContext context) {
        super(context);
        this.actions = actions;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}