package cool.ast.nodes;

import org.antlr.v4.runtime.ParserRuleContext;

public abstract class Expression extends ASTNode {
    public Expression(ParserRuleContext context) {
        super(context);
    }
}
