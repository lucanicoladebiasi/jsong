grammar JSong;

@header {
    package org.jsong.antlr;
}

jsong
    : exp* EOF
    ;

exp
    : array
    | obj
    | bool
    | nihil
    | number
    | text
    //
    | filter
    | map
    | path
    ;

array
    : '[' exp (',' exp)* ']'
    | '[' ']'
    ;

bool
    : TRUE
    | FALSE
    ;

filter
    : '|' exp '|'?
    ;

nihil
    : NULL
    ;

number
    : NUMBER
    ;

map
    : '.' exp
    ;

obj
    : '{' exp ':' exp (',' exp ':' exp)* '}'
    | '{' '}'
    ;

path
    : PATH
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

WS: [ \t\n\r]+ -> skip;