package cool.semantics.structures;

import org.antlr.v4.runtime.Token;

public class ResolutionResult {
    public TypeSymbol typeSymbol;
    public Token additionalInfo;


    public ResolutionResult(TypeSymbol typeSymbol, Token additionalInfo) {
        this.typeSymbol = typeSymbol;
        this.additionalInfo = additionalInfo;
    }
}
