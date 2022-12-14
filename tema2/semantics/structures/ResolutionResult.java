package cool.semantics.structures;

import org.antlr.v4.runtime.Token;

public class ResolutionResult {
    public ClassSymbol typeSymbol;
    public Token additionalInfo;
    public ResolutionResult(ClassSymbol typeSymbol, Token additionalInfo) {
        this.typeSymbol = typeSymbol;
        this.additionalInfo = additionalInfo;
    }
}
