package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public abstract class ASTNode {
    public ParserRuleContext context;

    protected boolean hasSemanticError = false;
    ASTNode(ParserRuleContext context) {
        this.context = context;
    }

    public boolean hasSemanticError() {
        return hasSemanticError;
    }

    public void setSemanticError() {
        this.hasSemanticError = true;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}
