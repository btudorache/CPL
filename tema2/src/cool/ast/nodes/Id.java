package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Id extends Expression {
    public Token name;

    protected Scope scope;
    public Id(Token name, ParserRuleContext context) {
        super(context);
        this.name = name;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
