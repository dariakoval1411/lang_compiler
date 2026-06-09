grammar Expr;

program
    : structDeclaration*
      classDeclaration*
      globalDeclaration*
      functionDeclaration*
      PROGRAM ID statement* END EOF
    ;

statement
    : declaration
    | structVariableDeclaration
    | assignment
    | inputStatement
    | showStatement
    | block
    | ifStatement
    | whileStatement
    ;

declaration
    : type ID ('[' INT_LITERAL ']')? ('=' expression)? ';'
    ;

structVariableDeclaration
    : ID ID ';'
    ;

assignment
    : ID ('[' expression ']')? '=' expression ';'
    | fieldAccess '=' expression ';'
    ;

inputStatement
    : INPUT '('expression')' ';'
    ;

showStatement
    : SHOW '('expression')' ';'
    ;

block
    : '{'statement*'}'
    ;

ifStatement
    : IF '('expression')' block (ELSE block)?
    ;

whileStatement
    : WHILE '('expression')' block
    ;

functionDeclaration
    : FUNCTION type ID '(' parameterList? ')' '{' statement* RETURN expression ';' '}'
    ;

functionCall
    : ID '(' argumentList? ')'
    ;

argumentList
    : expression (',' expression)*
    ;

parameterList
    : parameter (',' parameter)*
    ;

parameter
    : type ID
    ;

globalDeclaration
    : type ID ('[' INT_LITERAL ']')? ('=' literal)? ';'
    ;

structDeclaration
    : STRUCT ID '{' structField* '}'
    ;
classDeclaration
    : CLASS ID '{' structField* classMethodDeclaration*'}'
    ;

classMethodDeclaration
    : FUNCTION type ID '(' parameterList? ')' '{' statement* RETURN expression ';' '}'
    ;

structField
    : type ID ';'
    ;

fieldAccess
    : ID '.' ID
    ;

methodCall
    : ID '.' ID '(' argumentList? ')'
    ;

literal
    : INT_LITERAL
    | REAL_LITERAL
    ;

type
    : INT_TYPE
    | REAL_TYPE
    ;

expression
    : expression op=('*' | '/') expression
    | expression op=('+' | '-') expression
    | expression op=('<' | '>' | '<=' | '>=' | '==' | '!=') expression
    | methodCall
    | functionCall
    | fieldAccess
    | ID ('['expression']')?
    | '('expression')'
    | REAL_LITERAL
    | INT_LITERAL
    ;


PROGRAM : 'program';
END     : 'end';
INPUT   : 'input';
SHOW    : 'show';
IF      : 'if';
ELSE    : 'else';
WHILE   : 'while';
FUNCTION : 'function';
RETURN  : 'return';
STRUCT  : 'struct';
CLASS   : 'class';

INT_TYPE : 'int';
REAL_TYPE : 'real';

REAL_LITERAL : [0-9]+ '.' [0-9]+;
INT_LITERAL  : [0-9]+;

ID : [a-zA-Z_][a-zA-Z0-9_]*;
WS : [ \t\r\n]+ -> skip;