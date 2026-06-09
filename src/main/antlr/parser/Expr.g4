grammar Expr;

program
    : PROGRAM ID statement* END EOF
    ;

statement
    : declaration
    | assignment
    | inputStatement
    | showStatement
    ;

declaration
    : type ID ';'
    | type ID '=' expression ';'
    ;

assignment
    : ID '=' expression ';'
    ;

inputStatement
    : INPUT '(' ID ')' ';'
    ;

showStatement
    : SHOW '('expression')' ';'
    ;

type
    : INT_TYPE
    | REAL_TYPE
    ;

expression
    : expression op=('*' | '/') expression
    | expression op=('+' | '-') expression
    | '('expression')'
    | REAL_LITERAL
    | INT_LITERAL
    | ID
    ;


PROGRAM : 'program';
END     : 'end';
INPUT   : 'input';
SHOW    : 'show';

INT_TYPE : 'int';
REAL_TYPE : 'real';

REAL_LITERAL : [0-9]+ '.' [0-9]+;
INT_LITERAL  : [0-9]+;

ID : [a-zA-Z_][a-zA-Z0-9_]*;
WS : [ \t\r\n]+ -> skip;