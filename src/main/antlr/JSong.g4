grammar JSong;

@header {
    package org.jsong.antlr;
}

jsong
    : exp* EOF
    ;

add
    : '+' exp
    ;

array
    : '[' literal (',' literal)* ']'
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

div
    : '/' exp
    ;

exp
    : add
    | context
    | context_binding
    | div
    | filter
    | literal
    | map
    | mul
    | path
    | positional_binding
    | ranges
    | reminder
    | scope
    | sub
    ;

filter
    : '[' exp ']'
    ;

literal
    : array
    | obj
    | bool
    | nihil
    | number
    | text
    ;

map
    : '.' exp filter?
    ;

mul
    : '*' exp
    ;

nihil
    : NULL
    ;

number
    : NUMBER
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

reminder
    : '%' exp
    ;

scope:
    '(' exp (';'? exp)* ')'
    ;

sub
    : '-' exp
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