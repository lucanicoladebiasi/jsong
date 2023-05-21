/*
MIT License

Copyright (c) 2023 Luca Nicola Debiasi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

grammar JSong2;

@header {
    package io.github.lucanicoladebiasi.jsong1.antlr;
}

// PARSER RULES

exp_to_eof  :   exp* EOF
            ;

exp :   ID                                  # select
    |   MODULE                              # parent
    |   '*'                                 # fields
    |   '**'                                # descendants
    |   '$$'                                # root
    |   '->' exp                            # goto
    |   '.' exp                             # map
    |  '[' exp ']'                          # filter
    |   '{' (field (',' field)*)? '}'       # object
    |   (TRUE | FALSE)                      # boolean
    |   SUB exp                             # negative
    |   '(' (exp (';' (exp)?)*)? ')'        # block
    |   number                              # decimal
    |   STRING                              # string
    |   NULL                                # null
    ;

field   : key = exp ':' value += exp+;

number  : NUMBER;


// LEXER RULES

TRUE    : 'true';
FALSE   : 'false';

STRING  : '\'' (ESC | ~['\\])* '\''
	    | '"'  (ESC | ~["\\])* '"'
	    ;

NULL : 'null';

NUMBER  : INT '.' [0-9]+ EXP? // 1.35, 1.35E-9, 0.3
        | INT EXP             // 1e10 3e4
        | INT                 // 3, 45
        ;

FUNCTIONID : ('function' | 'λ') ;

WS      : [ \t\r\n]+ -> skip;              // ignore whitespace
COMMENT : '/*' .*? '*/' -> skip;      // allow comments

// Assign token names used in above grammar
GOTO    : '->';
CHAIN   : '~>' ;
ASSIGN  : ':=' ;
MUL     : '*' ;
DIV     : '/' ;
ADD     : '+' ;
SUB     : '-' ;
MODULE  : '%' ;
EQ      : '=' ;
NOT_EQ  : '!=' ;
LT      : '<' ;
LE      : '<=' ;
GT      : '>' ;
GE      : '>=' ;
CONCAT  : '&';
CIRCUM  : '^';
PIPE    : '|';
UNDER   : '_';
AND     : 'and';
OR      : 'or';


VAR_ID  : '$' ID ;

ID      : [\p{L}_] [\p{L}0-9_]*
	    | BACK_QUOTE ~[`]* BACK_QUOTE;


// LEXER FRAGMENTS

fragment ESC     :   '\\' (["'\\/bfnrt] | UNICODE) ;
fragment UNICODE : ([\u0080-\uFFFF] | 'u' HEX HEX HEX HEX) ;
fragment HEX     : [0-9a-fA-F] ;

fragment INT     : '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP     : [Ee] [+\-]? INT ;    // \- since - means "range" inside [...]

fragment SINGLE_QUOTE   : '\'';
fragment DOUBLE_QUOTE   : '"';
fragment BACK_QUOTE     : '`';
