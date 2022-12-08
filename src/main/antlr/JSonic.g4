grammar JSonic;

@header {
    package org.jsonic.antlr;
}

array
    : '[' exp (',' exp)* ']'
    | '[' ']'
    ;

bool
    : TRUE
    | FALSE
    ;

bool_op
    : AND
    | OR
    ;

comp_op
    : '='       #eq
    | '!='      #ne
    | '>'       #gt
    | '<'       #lt
    | '>='      #gte
    | '<='      #lte
    | 'in'      #in
    ;

exp
    : '?' yes = exp ':' no = exp                            #condition
    | '|' loc = exp ('|' upd = exp (',' del = exp)?)? '|'   #transform
    | '~>' exp                                              #chain
    | bool_op exp                                           #judge
    | math_op exp                                           #compute
    | comp_op exp                                           #compare
    | '&' exp                                               #concatenate
    | '^(' sort (',' sort)* ')'                             #orderby
    | '[' exp ']'                                           #filter
    | '$' label ':=' exp                                    #bind
    | '$$'                                                  #root
    | '$'                                                   #context
    | '%'                                                   #parent
    | label (obj)?                                          #select
    | json                                                  #literal
    | REGEX                                                 #regex
    ;

jsong
    : exp? EOF
    ;

json
    : array
    | obj
    | bool
    | nihil
    | number
    | text
    ;

label
    : LABEL
    ;

math_op
    : '*'   #mul
    | '/'   #div
    | '%'   #mod
    | '+'   #add
    | '-'   #sub
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
    : key = exp ':' value = exp
    ;

range
    : min = exp '..' max = exp
    ;

sort
    : '<' exp   #ascending
    | '>' exp   #descending
    | exp       #default
    ;

text
    : STRING
    ;

AND: 'and';
OR: 'or';

NULL: 'null';

TRUE: 'true';
FALSE: 'false';

REGEX: '/' (.)+? '/' 'i'? 'm'?;

LABEL: ([a-zA-Z][0-9a-zA-Z]*) | ('`' (.)+? '`');

NUMBER: '-'? INT ('.' [0-9] +)? EXP?;
fragment INT: '0' | [1-9] [0-9]*;
fragment EXP: [Ee] [+\-]? INT;

STRING: ('"' (ESC | SAFECODEPOINT)* '"' ) | ('\'' (ESC | SAFECODEPOINT)* '\'' );
fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
fragment SAFECODEPOINT: ~ ["\\\u0000-\u001F];

WS: [ \t\n\r]+ -> skip;


