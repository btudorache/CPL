package cool.ast.nodes;

import cool.ast.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class Case extends Expression {
    public Expression caseExpr;
    public List<CaseBranch> caseBranches;

    public Case(Expression caseExpr, List<CaseBranch> caseBranches, ParserRuleContext context) {
        super(context);
        this.caseExpr = caseExpr;
        this.caseBranches = caseBranches;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}