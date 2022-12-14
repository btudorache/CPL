package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.ClassSymbol;
import cool.semantics.structures.FunctionSymbol;
import cool.semantics.structures.GlobalScope;
import cool.semantics.structures.IdSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class FormalParam extends ASTNode {
    public Token type;
    public Token name;

    public GlobalScope globalScope;
    public FunctionSymbol scope;

    public IdSymbol symbol;
    public FormalParam(Token type, Token name, ParserRuleContext context) {
        super(context);
        this.type = type;
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
