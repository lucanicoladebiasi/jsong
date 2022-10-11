grammar JSong;

@header {
    package org.jsong.antlr;
}

jsong:
    exp? EOF
    ;

array:
    '[' literal (',' literal)* ']'  |
    '[' ']'
    ;

array_fun:
      APPEND
    | COUNT
    | DISTINCT
    | SORT
    | REVERSE
    | SHUFFLE
    | ZIP
    ;


bool:
      TRUE
    | FALSE
    ;

bool_fun:
      BOOLEAN
    | EXISTS
    | NOT
    ;

exp:
      literal                                               #json
    | '[' range (',' range)* ']'                            #ranges
    | LABEL                                                 #path
    | REGEX                                                 #regex
    | exp '[]'                                              #arrayConstructor
    | '$' LABEL                                             #variable
    | '$' LABEL ':=' exp                                    #variableBinding
    | '$'                                                   #context
    | '$$'                                                  #root
    | '**'                                                  #descendants
    | '*.' exp                                              #wildcardPrefix
    | exp '.*'                                              #wildcardPostfix
    | lhs = exp '.' rhs = exp ((CTX_BND | POS_BND) LABEL)?  #map
    | lhs = exp ('[' rhs = exp ']')                         #filter
    | array_fun '(' exp? (',' exp)* ')'                     #arrayFunction
    | bool_fun '(' exp? (',' exp)* ')'                      #booleanFunction
    | num_aggregate_fun '(' exp? (',' exp)* ')'             #numericAggregateFunction
    | num_fun '(' exp? (',' exp)* ')'                       #numericFunction
    | obj_fun '(' exp? (',' exp)* ')'                       #objectFunction
    | text_fun '(' exp? (',' exp)* ')'                      #textFunction
    | time_fun '(' exp? (',' exp)* ')'                      #timeFunction
    | lhs = exp '*' rhs = exp                               #mul
    | lhs = exp '/' rhs = exp                               #div
    | lhs = exp '%' rhs = exp                               #reminder
    | lhs = exp '+' rhs = exp                               #add
    | lhs = exp '-' rhs = exp                               #sub
    | lhs = exp '&' rhs = exp                               #concatenate
    | lhs = exp '=' rhs = exp                               #eq
    | lhs = exp '!=' rhs = exp                              #ne
    | lhs = exp '>' rhs = exp                               #gt
    | lhs = exp '<' rhs = exp                               #lt
    | lhs = exp '>=' rhs = exp                              #gte
    | lhs = exp '<='rhs = exp                               #lte
    | lhs = exp 'in' rhs = exp                              #in
    | lhs = exp 'and' rhs = exp                             #and
    | lhs = exp 'or' rhs = exp                              #or
    | '(' exp (';'? exp)* ')'                               #scope
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

obj_fun
    : ASSERT
    | EACH
    | ERROR
    | KEYS
    | LOOKUP
    | MERGE
    | SPREAD
    | TYPE
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

text_fun
    : CONTAINS
    | BASE64_DECODE
    | BASE64_ENCODE
    | DECODE_URL
    | DECODE_URL_COMPONENT
    | ENCODE_URL
    | ENCODE_URL_COMPONENT
    | EVAL
    | JOIN
    | LENGTH
    | LOWERCASE
    | MATCH
    | PAD
    | REPLACE
    | SPLIT
    | STRING_OF
    | SUBSTRING
    | SUBSTRING_AFTER
    | SUBSTRING_BEFORE
    | TRIM
    | UPPERCASE
    ;

time_fun
    : NOW
    | MILLIS
    | FROM_MILLIS
    | TO_MILLIS
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

// OBJECT FUNCTIONS

ASSERT: '$assert';
EACH:   '$each';
ERROR:  '$error';
KEYS:   '$keys';
LOOKUP: '$lookup';
MERGE:  '$merge';
SPREAD: '$spread';
TYPE:   '$type';

// TEXT FUNCTIONS

CONTAINS:               '$contains';
BASE64_DECODE:          '$base64decode';
BASE64_ENCODE:          '$base64encode';
DECODE_URL:             '$decodeUrl';
DECODE_URL_COMPONENT:   '$decodeUrlComponent';
ENCODE_URL:             '$encodeUrl';
ENCODE_URL_COMPONENT:   '$encodeUrlComponent';
EVAL:                   '$eval';
JOIN:                   '$join';
LENGTH:                 '$length';
LOWERCASE:              '$lowercase';
MATCH:                  '$match';
PAD:                    '$pad';
REPLACE:                '$replace';
SPLIT:                  '$split';
STRING_OF:              '$string';
SUBSTRING:              '$substring';
SUBSTRING_AFTER:        '$substringAfter';
SUBSTRING_BEFORE:       '$substringBefore';
TRIM:                   '$trim';
UPPERCASE:              '$uppercase';


// TIME FUNCTIONS

NOW: '$now';
MILLIS: '$millis';
FROM_MILLIS: '$fromMillis';
TO_MILLIS: '$toMillis';

// REGULAR EXPRESSIONS

REGEX: '/' (.)+? '/' 'i'? 'm'?;

// VARIABLE

CTX_BND: '@$';
POS_BND: '#$';

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