package cool.ast.nodes;

import cool.semantics.structures.ClassSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public abstract class Feature extends ASTNode {
    public Token name;
    public Token type;
    public Feature(Token name, Token type, ParserRuleContext context) {
        super(context);
        this.name = name;
        this.type = type;
    }
}