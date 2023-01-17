package cool.semantics.structures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cool.ast.nodes.ASTNode;
import org.antlr.v4.runtime.*;

import cool.compiler.Compiler;
import cool.parser.CoolParser;

public class SymbolTable {
    public static GlobalScope globals;
    
    private static boolean semanticErrors;
    
    public static void defineBasicClasses() {
        globals = new GlobalScope();
        semanticErrors = false;

        globals.add(ClassSymbol.OBJECT);
        globals.add(ClassSymbol.IO);
        globals.add(ClassSymbol.INT);
        globals.add(ClassSymbol.STRING);
        globals.add(ClassSymbol.BOOL);
        globals.add(ClassSymbol.SELF_TYPE);
    }
    
    /**
     * Displays a semantic error message.
     * 
     * @param ctx Used to determine the enclosing class context of this error,
     *            which knows the file name in which the class was defined.
     * @param info Used for line and column information.
     * @param str The error message.
     */
    public static void error(ParserRuleContext ctx, Token info, String str, ASTNode astNode) {
        while (!(ctx.getParent() instanceof CoolParser.ProgramContext)) {
            ctx = ctx.getParent();
        }

        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;
        
        System.err.println(message);
        astNode.setSemanticError();
        semanticErrors = true;
    }
    
    public static void error(String str) {
        String message = "Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static boolean hasSemanticErrors() {
        return semanticErrors;
    }
}
