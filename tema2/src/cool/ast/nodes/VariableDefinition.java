package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.ClassSymbol;
import cool.semantics.structures.IdSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class VariableDefinition extends Feature {
    public Expression variableValue;

    protected ClassSymbol scope;

    protected IdSymbol symbol;

    public VariableDefinition(Expression variableValue, Token name, Token type, ParserRuleContext context) {
        super(name, type, context);
        this.variableValue = variableValue;
    }

    public ClassSymbol getScope() {
        return scope;
    }

    public void setScope(ClassSymbol scope) {
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
