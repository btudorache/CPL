package cool.generator;

import cool.semantics.structures.ClassSymbol;
import org.stringtemplate.v4.ST;

public class CodeGenResult {
    public ST template;
    public ClassSymbol staticType;

    CodeGenResult(ST template) {
        this.template = template;
    }

    CodeGenResult(ST template, ClassSymbol staticType) {
        this.template = template;
        this.staticType = staticType;
    }
}
