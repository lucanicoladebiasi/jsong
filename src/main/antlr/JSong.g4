grammar JSong;

@header {
    package org.jsong.antlr;
}

jsong
    : exp? EOF
    ;


array
    : '[' literal (',' literal)* ']'
    | '[' ']'
    ;

bool
    : TRUE
    | FALSE
    ;

exp
    : lhs = exp '*' rhs = exp       #mul
    | lhs = exp '/' rhs = exp       #div
    | lhs = exp '%' rhs = exp       #reminder
    | lhs = exp '+' rhs = exp       #add
    | lhs = exp '-' rhs = exp       #sub
    | lhs = exp '=' rhs = exp       #eq
    | lhs = exp '!=' rhs = exp      #ne
    | lhs = exp '>' rhs = exp       #gt
    | lhs = exp '<' rhs = exp       #lt
    | lhs = exp '>=' rhs = exp      #gte
    | lhs = exp '<='rhs = exp       #lte
    | lhs = exp 'in' rhs = exp      #in
    | lhs = exp '[' rhs = exp ']'   #filter
    | lhs = exp '.' rhs = exp       #map
    | exp '[]'                      #arrayConstructor
    | '(' exp (';'? exp)* ')'       #scope
    | '[' range (',' range)* ']'    #ranges
    | PATH                          #path
    | literal                       #json
    | '$'                           #context
    ;

literal
    : array
    | obj
    | bool
    | nihil
    | number
    | text
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


range
    : min = exp '..' max = exp
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