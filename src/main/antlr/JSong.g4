grammar JSong;

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

exp
    //: lhs = exp '~>' rhs = exp                                          #chain
    //| '|' loc = exp ('|' upd = exp (',' del = exp)?)? '|'               #transform
    //| '^(' sort (',' sort)* ')'                                         #orderby
    : REGEX                                                             #regex
    | json                                                              #literal
    | path                                                              #select
    | '[' range (',' range)* ']'                                        #ranges
    | exp'[' ']'                                                        #expand
    | '$' label                                                         #lbl
    | fun                                                               #function
    | fun '(' (exp (',' exp)*)? ')'                                     #lambda
    | '$' label ':=' exp                                                #define
    | '$' label '(' (exp (',' exp)*)? ')'                               #call

    | lhs = exp'['  rhs = exp ']'                                       #filter
    | lhs = exp '.' rhs = exp                                           #map
    | lhs = exp '.' rhs = exp  POS label                                #mappos
    | lhs = exp '.' rhs = exp  CTX label                                #mapctx


    | lhs = exp '*' rhs = exp                                           #mul
    | lhs = exp '/' rhs = exp                                           #div
    | lhs = exp '%' rhs = exp                                           #mod
    | lhs = exp '+' rhs = exp                                           #add
    | lhs = exp '-' rhs = exp                                           #sub
    | lhs = exp '&' rhs = exp                                           #concatenate
    | lhs = exp 'in' rhs = exp                                          #in
    | lhs = exp '>'  rhs = exp                                          #gt
    | lhs = exp '<'  rhs = exp                                          #lt
    | lhs = exp '>=' rhs = exp                                          #gte
    | lhs = exp '<=' rhs = exp                                          #lte
    | lhs = exp '!=' rhs = exp                                          #ne
    | lhs = exp '='  rhs = exp                                          #eq
    | lhs = exp AND  rhs = exp                                          #and
    | lhs = exp OR   rhs = exp                                          #or
    | prd = exp '?' pos = exp ':' neg = exp                             #condition
    | '(' exp (';' exp)*')'                                             #scope
    ;

fun
    :  ('fun'|'function') '(' ('$' label (',' '$' label)*)? ')' '{' exp '}'
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
    : '$$'  #root
    | '$'   #context
    | '%'   #parent
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

CTX: '@$';
POS: '#$';

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


