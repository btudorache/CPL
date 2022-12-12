package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class InitCall extends Expression {
    public Token name;
    public List<Expression> args;

    public InitCall(Token name, List<Expression> args, ParserRuleContext context) {
        super(context);
        this.name = name;
        this.args = args;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}