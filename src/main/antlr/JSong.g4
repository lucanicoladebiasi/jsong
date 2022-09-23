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

array_fun
    : APPEND
    | COUNT
    | DISTINCT
    | SORT
    | REVERSE
    | SHUFFLE
    | ZIP
    ;


bool
    : TRUE
    | FALSE
    ;

bool_fun
      : BOOLEAN
      | EXISTS
      | NOT
      ;

exp
    : lhs = exp '*' rhs = exp                   #mul
    | lhs = exp '/' rhs = exp                   #div
    | lhs = exp '%' rhs = exp                   #reminder
    | lhs = exp '+' rhs = exp                   #add
    | lhs = exp '-' rhs = exp                   #sub
    | lhs = exp '&' rhs = exp                   #concatenate
    | lhs = exp '=' rhs = exp                   #eq
    | lhs = exp '!=' rhs = exp                  #ne
    | lhs = exp '>' rhs = exp                   #gt
    | lhs = exp '<' rhs = exp                   #lt
    | lhs = exp '>=' rhs = exp                  #gte
    | lhs = exp '<='rhs = exp                   #lte
    | lhs = exp 'in' rhs = exp                  #in
    | lhs = exp 'and' rhs = exp                 #and
    | lhs = exp 'or' rhs = exp                  #or
    | lhs = exp ('[' rhs = exp ']')             #filter
    | lhs = exp '.' rhs = exp ('#$'LABEL)?      #map
    | array_fun '(' exp? (',' exp)* ')'         #arrayFunction
    | bool_fun '(' exp? (',' exp)* ')'          #booleanFunction
    | num_aggregate_fun '(' exp? (',' exp)* ')' #numericAggregateFunction
    | num_fun '(' exp? (',' exp)* ')'           #numericFunction
    | exp '[]'                                  #arrayConstructor
    | exp '.*'                                  #wildcardPostfix
    | '(' exp (';'? exp)* ')'                   #scope
    | '[' range (',' range)* ']'                #ranges
    | '$' LABEL ':=' exp                        #variableBinding
    | '$' LABEL                                 #variable
    | LABEL                                     #path
    | literal                                   #json
    | '**'                                      #descendants
    | '*.' exp                                  #wildcardPrefix
    | '$$'                                      #root
    | '$'                                       #context
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

num_aggregate_fun
    : AVERAGE
    | SUM
    | MAX
    | MIN
    ;

num_fun
    : ABS
    | CEIL
    | FLOOR
    | FORMAT_BASE
    | FORMAT_INTEGER
    | FORMAT_NUMBER
    | NUMBER_OF
    | PARSE_INTEGER
    | POWER
    | RANDOM
    | ROUND
    | SQRT
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

// ARRAY FUNCTIONS

APPEND:     '$append';
COUNT:      '$count';
DISTINCT:   '$distinct';
SORT:       '$sort';
REVERSE:    '$reverse';
SHUFFLE:    '$shuffle';
ZIP:        '$zip';


// BOOLEAN FUNCTIONS

BOOLEAN:    '$boolean';
EXISTS:     '$exists';
NOT:        '$not';

// NUMERIC AGGREGATE FUNCTIONS

AVERAGE:    '$average';
SUM:        '$sum';
MAX:        '$max';
MIN:        '$min';

// NUMERIC FUNCTIONS

ABS:            '$abs';
CEIL:           '$ceil';
FLOOR:          '$floor';
FORMAT_BASE:    '$formatBase';
FORMAT_INTEGER: '$formatInteger';
FORMAT_NUMBER:  '$formatNumber';
NUMBER_OF:      '$number';
PARSE_INTEGER:  '$parseInteger';
POWER:          '$power';
RANDOM:         '$random';
ROUND:          '$round';
SQRT:           '$sqrt';

// JSON LITERALS

TRUE: 'true';

FALSE: 'false';

NULL: 'null';

NUMBER: '-'? INT ('.' [0-9] +)? EXP?;
fragment INT: '0' | [1-9] [0-9]*;
fragment EXP: [Ee] [+\-]? INT;

LABEL: ([a-zA-Z][0-9a-zA-Z]*) | ('`' (.)+? '`');

STRING: ('"' (ESC | SAFECODEPOINT)* '"' ) | ('\'' (ESC | SAFECODEPOINT)* '\'' );
fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
fragment SAFECODEPOINT: ~ ["\\\u0000-\u001F];

GREATER: '>';
LESS: '<';

WS: [ \t\n\r]+ -> skip;