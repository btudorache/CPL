package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.IdSymbol;
import cool.semantics.structures.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class LocalParam extends ASTNode {
    public Token name;
    public Token type;
    public Expression value;

    protected Scope scope;

    protected IdSymbol symbol;
    public LocalParam(Token name, Token type, Expression value, ParserRuleContext context) {
        super(context);
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public IdSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(IdSymbol symbol) {
        this.symbol = symbol;
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