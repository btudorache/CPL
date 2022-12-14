lexer grammar CoolLexer;

tokens { ERROR }

@header{
    package cool.lexer;
    import java.util.regex.Pattern;
}

@members{
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }

    private void processString() {
        String parsedString = getText();
        if (parsedString.length() > 1024) {
            raiseError("String constant too long");
            return;
        }

        if (parsedString.contains("\u0000")) {
            raiseError("String contains null character");
            return;
        }

        String trimmed = parsedString.substring(1, parsedString.length() - 1);
        Pattern specialCharPattern = Pattern.compile("\\\\[ntbfr]");
        String replacedSpecialChar = specialCharPattern.matcher(trimmed).replaceAll(mr -> switch ( mr.group(0)) {
            case "\\n" -> "\n";
            case "\\t" -> "\t";
            case "\\b" -> "\b";
            case "\\f" -> "\f";
            case "\\r" -> "\r";
            default -> "";
        });

        Pattern anyCharPattern = Pattern.compile("\\\\[\\w|\\s]");
        String finalString = anyCharPattern.matcher(replacedSpecialChar).replaceAll(mr -> mr.group(0).substring(1));
        setText(finalString);
    }
}

BOOL : 'true' | 'false';

IF: 'if';

THEN: 'then';

ELSE: 'else';

FI: 'fi';

CLASS: 'class';

INHERITS: 'inherits';

IN: 'in';

ISVOID: 'isvoid';

LET: 'let';

LOOP: 'loop';

POOL: 'pool';

WHILE: 'while';

CASE: 'case';

ESAC: 'esac';

NEW: 'new';

OF: 'of';

NOT: 'not';

fragment SELF: 'self';

fragment SELF_TYPE: 'SELF_TYPE';

TYPE : ([A-Z] (LETTER | '_' | DIGIT)*);

fragment LETTER : [a-zA-Z];
ID : ((LETTER | '_')(LETTER | '_' | DIGIT)* | SELF | SELF_TYPE);

fragment DIGIT : [0-9];
INT : DIGIT+;

STRING: '"'
        ('\\"' | '\\' (' ')* NEW_LINE | . )*?
        ('"' { processString(); } | ~'\\' (' ')* NEW_LINE { raiseError("Unterminated string constant"); } | EOF { raiseError("EOF in string constant"); });

COMPLEMENT: '~';

DOT: '.';

AT: '@';

COLON : ':';

SEMICOLON : ';';

COMMA : ',';

ASSIGN : '<-';

LPAREN : '(';

RPAREN : ')';

LBRACE : '{';

RBRACE : '}';

PLUS : '+';

MINUS : '-';

MULT : '*';

DIV : '/';

EQUAL : '=';

LT : '<';

LE : '<=';

CASE_BRANCH: '=>';

fragment NEW_LINE : '\r'? '\n';

LINE_COMMENT: '--' .*? (NEW_LINE | EOF) -> skip;

UNMATCHED_BLOCK_COMMENT: ('*)' | BLOCK_COMMENT '*)') { raiseError("Unmatched *)"); };

BLOCK_COMMENT: '(*' (BLOCK_COMMENT | .)*? ('*)' { skip(); } | EOF { raiseError("EOF in comment"); });

WS : [ \n\f\r\t]+ -> skip;

INVALID_CHARACTER: . { raiseError("Invalid character: " + getText()); };
