package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.FunctionSymbol;
import cool.semantics.structures.GlobalScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class FunctionDefinition extends Feature {
    public Expression functionValue;
    public List<FormalParam> formalParams;

    public GlobalScope globalScope;

    public FunctionSymbol scope;
    public FunctionDefinition(Expression functionValue, List<FormalParam> formalParams, Token name, Token type, ParserRuleContext context) {
        super(name, type, context);
        this.functionValue = functionValue;
        this.formalParams = formalParams;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}