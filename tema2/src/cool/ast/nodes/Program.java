package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class Program extends ASTNode {
    public List<Class> classes;

    public Program(List<Class> classes, ParserRuleContext context) {
        super(context);
        this.classes = classes;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}