grammar X;

@header {
    package org.jsong.antlr;
}

input:
  exp* EOF;

array
    : '[' literal (',' literal)* ']'
    | '[' ']'
    ;

bool
    : TRUE
    | FALSE
    ;

exp
    : lhs = exp '.' rhs = exp                               #map
    | lgs = exp '[' rhs = exp ']'                           #filter
    | lhs = exp '=' rhs = exp                               #eq
    | label                                                 #var
    | literal                                               #json
    ;

label
    : LABEL
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