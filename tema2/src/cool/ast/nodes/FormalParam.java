package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.FunctionSymbol;
import cool.semantics.structures.IdSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class FormalParam extends ASTNode {
    public Token type;
    public Token name;

    protected FunctionSymbol scope;

    protected IdSymbol symbol;
    public FormalParam(Token type, Token name, ParserRuleContext context) {
        super(context);
        this.type = type;
        this.name = name;
    }

    public FunctionSymbol getScope() {
        return scope;
    }

    public void setScope(FunctionSymbol scope) {
        this.scope = scope;
    }

    public IdSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(IdSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
