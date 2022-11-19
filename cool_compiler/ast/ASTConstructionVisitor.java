package cool.ast;

import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;

import java.util.stream.Collectors;

public class ASTConstructionVisitor extends CoolParserBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        var classes = ctx.classes.stream().map(classNode -> (Class) visit(classNode)).collect(Collectors.toList());
        return new Program(classes, ctx.start);
    }

    @Override
    public ASTNode visitClass(CoolParser.ClassContext ctx) {
        var features = (ctx.features != null) ?
                                    ctx.features.stream().map(feature -> (Feature) visit(feature)).collect(Collectors.toList()) :
                                    null;
        return new Class(ctx.name, ctx.inherits, features, ctx.start);
    }

    @Override
    public ASTNode visitFuncDef(CoolParser.FuncDefContext ctx) {
        var formalParams = (ctx.formals != null) ?
                            ctx.formals.stream().map(formal -> (FormalParam) visit(formal)).collect(Collectors.toList()) :
                            null;
        return new FunctionDefinition((Expression) visit(ctx.funcDefExpr), formalParams, ctx.ID().getSymbol(), ctx.TYPE().getSymbol(), ctx.start);
    }

    @Override
    public ASTNode visitVarDef(CoolParser.VarDefContext ctx) {
        Expression varExpr = (ctx.varDefExpr != null) ? (Expression) visit(ctx.varDefExpr) : null;
        return new VariabileDefinition(varExpr, ctx.ID().getSymbol(), ctx.TYPE().getSymbol(), ctx.start);
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        return new FormalParam(ctx.type, ctx.name, ctx.start);
    }

    @Override
    public ASTNode visitLocal(CoolParser.LocalContext ctx) {
        var value = (ctx.value != null) ? (Expression) visit(ctx.value) : null;
        return new LocalParam(ctx.ID().getSymbol(), ctx.TYPE().getSymbol(), value, ctx.start);
    }

    @Override
    public ASTNode visitCaseBranch(CoolParser.CaseBranchContext ctx) {
        return new CaseBranch(ctx.ID().getSymbol(), ctx.TYPE().getSymbol(), (Expression) visit(ctx.value), ctx.start);
    }

    @Override
    public ASTNode visitNew(CoolParser.NewContext ctx) {
        return new New(ctx.TYPE().getSymbol(), ctx.start);
    }

    @Override
    public ASTNode visitPlusMinus(CoolParser.PlusMinusContext ctx) {
        return new BinaryOperator((Expression) visit(ctx.left), (Expression) visit(ctx.right), ctx.op, ctx.start);
    }

    @Override
    public ASTNode visitBool(CoolParser.BoolContext ctx) {
        return new BoolNode(ctx.start);
    }

    @Override
    public ASTNode visitString(CoolParser.StringContext ctx) {
        return new StringNode(ctx.start);
    }

    @Override
    public ASTNode visitIsvoid(CoolParser.IsvoidContext ctx) {
        return new UnaryOperator((Expression) visit(ctx.expr()), ctx.ISVOID().getSymbol(), ctx.start);
    }

    @Override
    public ASTNode visitInitcall(CoolParser.InitcallContext ctx) {
        var args = (ctx.args != null) ?
                                  ctx.args.stream().map(arg -> (Expression) visit(arg)).collect(Collectors.toList()) :
                                  null;
        return new InitCall(ctx.ID().getSymbol(), args, ctx.start);
    }

    @Override
    public ASTNode visitWhile(CoolParser.WhileContext ctx) {
        return new While((Expression) visit(ctx.cond),
                         (Expression) visit(ctx.action),
                         ctx.start);
    }

    @Override
    public ASTNode visitInt(CoolParser.IntContext ctx) {
        return new Int(ctx.INT().getSymbol());
    }

    @Override
    public ASTNode visitCall(CoolParser.CallContext ctx) {
        var atType = (ctx.TYPE()) != null ? ctx.TYPE().getSymbol() : null;
        var args = (ctx.args != null) ?
                ctx.args.stream().map(arg -> (Expression) visit(arg)).collect(Collectors.toList()) :
                null;
        return new Call((Expression) visit(ctx.prefix), atType, ctx.ID().getSymbol(), args, ctx.start);
    }

    @Override
    public ASTNode visitNot(CoolParser.NotContext ctx) {
        return new UnaryOperator((Expression) visit(ctx.expr()), ctx.NOT().getSymbol(), ctx.start);
    }

    @Override
    public ASTNode visitParen(CoolParser.ParenContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ASTNode visitMultDiv(CoolParser.MultDivContext ctx) {
        return new BinaryOperator((Expression) visit(ctx.left), (Expression) visit(ctx.right), ctx.op, ctx.start);
    }

    @Override
    public ASTNode visitUnaryMinus(CoolParser.UnaryMinusContext ctx) {
        return new UnaryOperator((Expression) visit(ctx.expr()), ctx.MINUS().getSymbol(), ctx.start);
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        return new Block(ctx.actions.stream().map(action -> (Expression) visit(action)).collect(Collectors.toList()), ctx.start);
    }

    @Override
    public ASTNode visitLet(CoolParser.LetContext ctx) {
        var localParams = ctx.localParams.stream().map(local -> (LocalParam) visit(local)).collect(Collectors.toList());
        return new Let(localParams, (Expression) visit(ctx.action), ctx.start);
    }

    @Override
    public ASTNode visitRelational(CoolParser.RelationalContext ctx) {
        return new BinaryOperator((Expression) visit(ctx.left), (Expression) visit(ctx.right), ctx.op, ctx.start);
    }

    @Override
    public ASTNode visitId(CoolParser.IdContext ctx) {
        return new Id(ctx.ID().getSymbol());
    }

    @Override
    public ASTNode visitComplement(CoolParser.ComplementContext ctx) {
        return new UnaryOperator((Expression) visit(ctx.expr()), ctx.COMPLEMENT().getSymbol(), ctx.start);
    }

    @Override
    public ASTNode visitIf(CoolParser.IfContext ctx) {
        return new If((Expression) visit(ctx.cond),
                      (Expression) visit(ctx.thenBranch),
                      (Expression) visit(ctx.elseBranch),
                      ctx.start);
    }

    @Override
    public ASTNode visitCase(CoolParser.CaseContext ctx) {
        return new Case((Expression) visit(ctx.caseValue),
                        ctx.branches.stream().map(branch -> (CaseBranch) visit(branch)).collect(Collectors.toList()),
                        ctx.start);
    }

    @Override
    public ASTNode visitAssign(CoolParser.AssignContext ctx) {
        return new Assign(ctx.name, (Expression) visit(ctx.expr()), ctx.start);
    }
}
