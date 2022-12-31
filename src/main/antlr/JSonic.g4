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


exp
    : '(' exp (';' exp)*')'                                             #scope
    | prd = exp '?' pos = exp ':' neg = exp                             #condition
    | lhs = exp '~>' rhs = exp                                          #chain
    | '|' loc = exp ('|' upd = exp (',' del = exp)?)? '|'               #transform
    | '$' label ':=' exp                                                #define
    | '$' label ( '(' (exp (',' exp)*)? ')')?                           #call
    | 'fun'('ction')? '(' ('$' label (',' '$' label)*)? ')' '{' exp '}' #function
    | '^(' sort (',' sort)* ')'                                         #orderby
    | exp'[' ']'                                                        #expand
    | lhs = exp'['  rhs = exp ']'                                       #filter
    | lhs = exp '.' rhs = exp                                           #map
    | lhs = exp '&' rhs = exp                                           #concatenate
    | lhs = exp '*' rhs = exp                                           #mul
    | lhs = exp '/' rhs = exp                                           #div
    | lhs = exp '%' rhs = exp                                           #mod
    | lhs = exp '+' rhs = exp                                           #add
    | lhs = exp '-' rhs = exp                                           #sub
    | lhs = exp '='  rhs = exp                                          #eq
    | lhs = exp '!=' rhs = exp                                          #ne
    | lhs = exp '>'  rhs = exp                                          #gt
    | lhs = exp '<'  rhs = exp                                          #lt
    | lhs = exp '>=' rhs = exp                                          #gte
    | lhs = exp '<=' rhs = exp                                          #lte
    | lhs = exp 'in' rhs = exp                                          #in
    | lhs = exp AND  rhs = exp                                          #and
    | lhs = exp OR   rhs = exp                                          #or
    | '[' range (',' range)* ']'                                        #ranges
    | path                                                              #select
    | json                                                              #literal
    | REGEX                                                             #regex
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


