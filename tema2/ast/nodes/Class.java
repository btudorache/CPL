package cool.ast.nodes;

import cool.ast.ASTVisitor;
import cool.semantics.structures.ClassSymbol;
import cool.semantics.structures.Scope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class Class extends ASTNode {
    public Token name;
    public Token inherits;
    public List<Feature> features;
    public ClassSymbol scope = null;

    public Class(Token name, Token inherits, List<Feature> features, ParserRuleContext context) {
        super(context);
        this.features = features;
        this.inherits = inherits;
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
