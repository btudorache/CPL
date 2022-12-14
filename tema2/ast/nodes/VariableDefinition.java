package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.ClassSymbol;
import cool.semantics.structures.GlobalScope;
import cool.semantics.structures.IdSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class VariableDefinition extends Feature {
    public Expression variableValue;

    public ClassSymbol scope;

    public GlobalScope globalScope;

    public IdSymbol symbol;

    public VariableDefinition(Expression variableValue, Token name, Token type, ParserRuleContext context) {
        super(name, type, context);
        this.variableValue = variableValue;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
