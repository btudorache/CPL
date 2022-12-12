package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class Call extends Expression {
    public Expression prefix;
    public Token atType;
    public Token name;
    public List<Expression> args;

    public Call(Expression prefix, Token atType, Token name, List<Expression> args, ParserRuleContext context) {
        super(context);
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