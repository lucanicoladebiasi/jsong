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

//args    : '(' (VAR_ID (',' VAR_ID)*)* ')';

element :   exp | range;

exp
        :   '.' exp                                                         # map
        |   '[' exp ']'                                                     # filter
        |   lhs = exp op = (STAR | SLASH | PERCENT) rhs = exp               # evalMulDivMod
        |   lhs = exp op = (PLUS | DASH) rhs = exp                          # evalSumSub
        |   lhs = exp AMP rhs = exp                                         # concatenate
        |   lhs = exp op = (LT | LE | GE | GT | NE | EQ | IN ) rhs = exp    # compare
        |   lhs = exp op = (AND | OR) rhs = exp                             # evalAndOr
        |   DASH exp                                                        # evalNegate
        |   AT VAR_ID                                                       # bindContext
        |   HASH VAR_ID                                                     # bindPosition
        |   VAR_ID                                                          # callVariable
        |   path                                                            # select
        |   type                                                            # literal
        ;

//

field   :   key = exp ':' val = exp;

type    :   '[' element? (',' element)* ']'         # array
        |   '{' field? (',' field)* '}'             # object
        |   SLASH pattern (SLASH | REG_CI | REG_ML) # regex
        |   STRING                                  # text
        |   NUMBER                                  # number
        |   FALSE                                   # false
        |   TRUE                                    # true
        |   NULL                                    # null
        ;

path    :   PERCENT (DOT PERCENT)*  # parent
        |   DOLLAR DOLLAR           # root
        |   DOLLAR                  # context
        |   STAR                    # wildcard
        |   STAR STAR               # descendants
        |   ID                      # id
        ;

range  :   lhs = exp '..' rhs = exp;

pattern :  (~'/' | '\\' '/' '?')*;

// LEXER RULES


AT      : '@';
HASH    : '#';

LT      : '<';
LE      : '<=';
GT      : '>';
GE      : '>=';
NE      : '!=';
EQ      : '=';
IN      : 'in';

AMP     : '&';
DASH    : '-';
DOLLAR  : '$';
DOT     : '.';
PERCENT : '%';
PLUS    : '+';
SLASH   : '/';
STAR    : '*';

REG     : SLASH;
REG_CI  : SLASH 'i';
REG_ML  : SLASH 'm';

FUNC    : ('function' | 'fun' | 'λ') ;

NULL    : 'null';

AND     : 'and';
OR      : 'or';

FALSE   : 'false';
TRUE    : 'true';

VAR_ID  : '$' ID;

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
