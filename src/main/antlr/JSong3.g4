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

grammar JSong3;

@header {
    package io.github.lucanicoladebiasi.jsong.antlr;
}

// PARSER RULES

jsong   :   exp* EOF;

args    :   '(' (var (',' var)*)* ')';

cvb     :   '@' var;

element :   exp | range;

exp     :   '(' exp (';' exp?)* ')'                                     # block
        |   lhs = exp '[' rhs = exp ']'                                 # filter
        |   lhs = exp '.' rhs = exp pvb cvb                             # mapPvbCvb
        |   lhs = exp '.' rhs = exp pvb                                 # mapPvb
        |   lhs = exp '.' rhs = exp cvb                                 # mapCvb
        |   lhs = exp '.' rhs = exp                                     # map
        |   lhs = exp op = (DIV | MOD | MUL) rhs = exp                  # evalDivModMul
        |   lhs = exp op = (SUM | SUB) rhs = exp                        # evalSumSub
        |   lhs = exp '&' rhs = exp                                     # evalConcatenate
        |   lhs = exp op = (LT | LE | GE | GT | NE | EQ | IN) rhs = exp # evalCompare
        |   lhs = exp op = (AND | OR) rhs = exp                         # evalAndOr
        |   '-' exp                                                     # evalNegative
        |   var                                                         # callVariable
        |   path                                                        # select
        |   type                                                        # literal
        ;

field   :   key = exp ':' val = exp;

type    :   '[' element? (',' element)* ']'     # array
        |   '{' field? (',' field)* '}'         # object
        |   '/' pattern ('/' | REG_CI | REG_ML) # regex
        |   STRING                              # text
        |   NUMBER                              # number
        |   FALSE                               # false
        |   TRUE                                # true
        |   NULL                                # null
        ;

path    :   step = '%' ('.' step = '%')*        # callParent
        |   '$$'                                # callRoot
        |   '$'                                 # callContext
        |   '*'                                 # callWildcard
        |   '**'                                # callDescendants
        |   ID                                  # id
        ;

pvb     :   '#' var;

range   :   lhs = exp '..' rhs = exp;

pattern :   (~'/' | '\\' '/' '?')*;

var     :   '$' ID;


// LEXER RULES

EQ      : '=';
GE      : '>=';
GT      : '>';
LE      : '<=';
LT      : '<';
IN      : 'in';
NE      : '!=';

DIV     : '/';
MUL     : '*';
MOD     : '%';
SUB     : '-';
SUM     : '+';

REG_CI  : '/i';
REG_ML  : '/m';

FUNC    : ('function' | 'fun' | 'λ') ;

NULL    : 'null';

AND     : 'and';
OR      : 'or';

FALSE   : 'false';
TRUE    : 'true';

ID      : [\p{L}_] [\p{L}0-9_]*
	    | BACK_QUOTE ~[`]* BACK_QUOTE;

NUMBER  : INT '.' [0-9]+ EXP? // 1.35, 1.35E-9, 0.3
        | INT EXP             // 1e10 3e4
        | INT                 // 3, 45
        ;

STRING  : '\'' (ESC | ~['\\])* '\''
	    | '"'  (ESC | ~["\\])* '"'
	    ;

WS      : [ \t\r\n]+ -> skip;         // ignore whitespace
COMMENT : '/*' .*? '*/' -> skip;      // allow comments

// LEXER FRAGMENTS

fragment ESC     :   '\\' (["'\\/bfnrt] | UNICODE) ;
fragment UNICODE : ([\u0080-\uFFFF] | 'u' HEX HEX HEX HEX) ;
fragment HEX     : [0-9a-fA-F] ;

fragment INT     : '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP     : [Ee] [+\-]? INT ;    // \- since - means "range" inside [...]

fragment SINGLE_QUOTE   : '\'';
fragment DOUBLE_QUOTE   : '"';
fragment BACK_QUOTE     : '`';
