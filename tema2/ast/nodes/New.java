package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class New extends Expression {
    public Token type;

    public Scope scope = null;

    public New(Token type, ParserRuleContext context) {
        super(context);
        this.type = type;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
