package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.Scanner;

public class CaseBranch extends ASTNode {
    public Token name;
    public Token type;
    public Expression value;

    protected Scope scope;
    public CaseBranch(Token name, Token type, Expression value, ParserRuleContext context) {
        super(context);
        this.name = name;
        this.type = type;
        this.value = value;
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