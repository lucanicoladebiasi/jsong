grammar JSong;

@header {
    package org.jsong.antlr;
}

jsong
    : (exp | map)* EOF
    ;

literal
    : array
    | obj
    | bool
    | nihil
    | number
    | text
    ;

array
    : '[' exp (',' exp)* ']'
    | '[' ']'
    ;

bool
    : TRUE
    | FALSE
    ;

context
    : '$'
    ;

context_binding
    : '@' context path
    ;

exp
    : context
    | context_binding
    | literal
    | path
    | positional_binding
    | ranges
    | scope
    ;


nihil
    : NULL
    ;

number
    : NUMBER
    ;

map
    : lhs = exp '.' rhs = exp '[' filter = exp ']'
    ;

obj
    : '{' pair (',' pair)* '}'
    | '{' '}'
    ;

pair
    : lhs = exp ':' rhs = exp
    ;

path
    : PATH
    ;

positional_binding
    : '#' context path
    ;

range
    : min = exp '..' max = exp
    ;

ranges
    : '[' range (',' range)* ']'
    ;

scope:
    '(' exp (';'? exp)* ')'
    ;

text
    : STRING
    ;

TRUE: 'true';

FALSE: 'false';

NULL: 'null';

NUMBER: '-'? INT ('.' [0-9] +)? EXP?;
fragment INT: '0' | [1-9] [0-9]*;
fragment EXP: [Ee] [+\-]? INT;

PATH: ([a-zA-Z][0-9a-zA-Z]*) | ('`' (.)+? '`');

STRING: ('"' (ESC | SAFECODEPOINT)* '"' ) | ('\'' (ESC | SAFECODEPOINT)* '\'' );
fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
fragment SAFECODEPOINT: ~ ["\\\u0000-\u001F];

GREATER: '>';
LESS: '<';

WS: [ \t\n\r]+ -> skip;