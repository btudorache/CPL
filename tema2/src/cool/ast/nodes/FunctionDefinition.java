package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.FunctionSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class FunctionDefinition extends Feature {
    public Expression functionValue;
    public List<FormalParam> formalParams;

    protected FunctionSymbol scope;
    public FunctionDefinition(Expression functionValue, List<FormalParam> formalParams, Token name, Token type, ParserRuleContext context) {
        super(name, type, context);
        this.functionValue = functionValue;
        this.formalParams = formalParams;
    }

    public FunctionSymbol getScope() {
        return scope;
    }

    public void setScope(FunctionSymbol scope) {
        this.scope = scope;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}