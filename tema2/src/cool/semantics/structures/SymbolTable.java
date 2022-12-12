package cool.semantics.structures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cool.ast.nodes.ASTNode;
import org.antlr.v4.runtime.*;

import cool.compiler.Compiler;
import cool.parser.CoolParser;

public class SymbolTable {
    public static Scope globals;
    
    private static boolean semanticErrors;
    
    public static void defineBasicClasses() {
        globals = new DefaultScope(null);
        semanticErrors = false;

        ClassSymbol objectClass = new ClassSymbol(null, "Object");
        objectClass.add(new FunctionSymbol(objectClass, "abort", TypeSymbol.OBJECT));
        objectClass.add(new FunctionSymbol(objectClass, "type_name", TypeSymbol.STRING));
        objectClass.add(new FunctionSymbol(objectClass, "copy", TypeSymbol.SELF_TYPE));
        globals.add(objectClass);



        ClassSymbol ioClass = new ClassSymbol(objectClass, "IO");
        ioClass.setInheritedScope(objectClass);

        FunctionSymbol ioOutString = new FunctionSymbol(ioClass, "out_string", TypeSymbol.SELF_TYPE);
        ioOutString.add(new IdSymbol("x", TypeSymbol.STRING));
        ioClass.add(ioOutString);

        FunctionSymbol ioOutInt = new FunctionSymbol(ioClass, "out_int", TypeSymbol.SELF_TYPE);
        ioOutInt.add(new IdSymbol("x", TypeSymbol.INT));
        ioClass.add(ioOutInt);

        ioClass.add(new FunctionSymbol(ioClass, "in_string", TypeSymbol.STRING));
        ioClass.add(new FunctionSymbol(ioClass, "in_int", TypeSymbol.INT));
        globals.add(ioClass);



        ClassSymbol intClass = new ClassSymbol(objectClass, "Int");
        intClass.setInheritedScope(objectClass);
        globals.add(intClass);



        ClassSymbol stringClass = new ClassSymbol(objectClass, "String");
        stringClass.setInheritedScope(objectClass);

        stringClass.add(new FunctionSymbol(stringClass, "length", TypeSymbol.INT));

        FunctionSymbol stringConcat = new FunctionSymbol(stringClass, "concat", TypeSymbol.STRING);
        stringConcat.add(new IdSymbol("s", TypeSymbol.STRING));
        stringClass.add(stringConcat);

        FunctionSymbol stringSubstr = new FunctionSymbol(stringClass, "substr", TypeSymbol.STRING);
        stringSubstr.add(new IdSymbol("i", TypeSymbol.INT));
        stringSubstr.add(new IdSymbol("l", TypeSymbol.INT));
        stringClass.add(stringSubstr);
        globals.add(stringClass);



        ClassSymbol boolClass = new ClassSymbol(objectClass, "Bool");
        boolClass.setInheritedScope(objectClass);
        globals.add(boolClass);
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
        while (! (ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();
        
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
