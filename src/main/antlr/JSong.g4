grammar jsong;

@header {
    package org.jsong.antlr;
}

array
    : '[' exp (',' exp)* ']'
    | '[' ']'
    ;

bool
    : TRUE
    | FALSE
    ;

exp
    : NUMBER
    | STRING
    | NULL
    | bool
    | array
    | object
    ;

object
    : '{' key = exp ':' value = exp (',' key = exp ':' value = exp)* '}'
    | '{' '}'
    ;

FALSE: 'false';

NULL: 'null';

NUMBER: '-'? INT ('.' [0-9] +)? EXP?;
fragment INT: '0' | [1-9] [0-9]*;
fragment EXP: [Ee] [+\-]? INT;

STRING: ('"' (ESC | SAFECODEPOINT)* '"' ) | ('\'' (ESC | SAFECODEPOINT)* '\'' );
fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
fragment SAFECODEPOINT: ~ ["\\\u0000-\u001F];

TRUE: 'true';

WS: [ \t\n\r]+ -> skip;