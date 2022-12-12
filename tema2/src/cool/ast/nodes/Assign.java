package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Assign extends Expression {
    public Token name;
    public Expression expr;

    protected Scope scope;

    public Assign(Token name, Expression expr, ParserRuleContext context) {
        super(context);
        this.name = name;
        this.expr = expr;
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