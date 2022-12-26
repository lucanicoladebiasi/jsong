grammar JSonic;

@header {
    package org.jsong.antlr;
}

jsong
    : exp* EOF
    ;

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
    : '(' exp (';' exp)* ')'                                            #scope
    | '?' yes = exp ':' no = exp                                        #condition
    | '~>' exp                                                          #chain
    | '|' loc = exp ('|' upd = exp (',' del = exp)?)? '|'               #transform
    | '$' label ':=' exp                                                #define
    | '$' label ( '(' (exp (',' exp)*)? ')')?                           #call
    | 'fun'('ction')? '(' ('$' label (',' '$' label)*)? ')' '{' exp '}' #function
    | '^(' sort (',' sort)* ')'                                         #orderby
//    | '.' '[' exp ']'                                                   #mapArray
    | '[' exp ']'                                                       #filter
    | '.' exp                                                           #map
    | '&' exp                                                           #concatenate
    | math_op exp                                                       #compute
    | comp_op exp                                                       #compare
    | bool_op exp                                                       #judge
    | '[' range (',' range)* ']'                                        #ranges
    | path                                                              #select
    | json                                                              #literal
    | REGEX                                                             #regex
    | '$$'                                                              #root
    | '$'                                                               #context
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

path
    : '%'   #parent
    | '*'   #all
    | '**'  #descendants
    | label #field
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


