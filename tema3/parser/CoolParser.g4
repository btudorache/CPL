parser grammar CoolParser;

options {
    tokenVocab = CoolLexer;
}

@header{
    package cool.parser;
}

program
    :   (classes+=class SEMICOLON)+
    ;

class
    : CLASS name=TYPE (INHERITS inherits=TYPE)? LBRACE (features+=feature SEMICOLON)* RBRACE
    ;

feature
    : ID LPAREN (formals+=formal (COMMA formals+=formal)*)? RPAREN COLON TYPE LBRACE funcDefExpr=expr RBRACE # funcDef
    | ID COLON TYPE (ASSIGN varDefExpr=expr)?                                                                # varDef
    ;

formal
    :   name=ID COLON type=TYPE
    ;

expr
    :   prefix=expr (AT TYPE)? DOT ID LPAREN (args+=expr (COMMA args+=expr)*)? RPAREN     # call
    |   ID LPAREN (args+=expr (COMMA args+=expr)*)? RPAREN                                # initcall
    |   IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI                         # if
    |   WHILE cond=expr LOOP action=expr POOL                                             # while
    |   LBRACE (actions+=expr SEMICOLON)+ RBRACE                                          # block
    |   LET localParams+=local (COMMA localParams+=local)* IN action=expr                 # let
    |   CASE caseValue=expr OF (branches+=caseBranch)+ ESAC                               # case
    |   COMPLEMENT expr                                                                   # complement
    |   ISVOID expr                                                                       # isvoid
    |   NEW TYPE                                                                          # new
    |   MINUS expr                                                                        # unaryMinus
    |   left=expr op=(MULT | DIV) right=expr                                              # multDiv
    |   left=expr op=(PLUS | MINUS) right=expr                                            # plusMinus
    |   left=expr op=(LT | LE | EQUAL) right=expr                                         # relational
    |   NOT expr                                                                          # not
    |   name=ID ASSIGN expr                                                               # assign
    |   LPAREN e=expr RPAREN                                                              # paren
    |   ID                                                                                # id
    |   INT                                                                               # int
    |   BOOL                                                                              # bool
    |   STRING                                                                            # string
    ;

local
    :   ID COLON TYPE (ASSIGN value=expr)?
    ;

caseBranch
    : ID COLON TYPE CASE_BRANCH value=expr SEMICOLON
    ;

