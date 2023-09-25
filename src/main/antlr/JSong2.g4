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
    package io.github.lucanicoladebiasi.jsong.antlr;
}

// PARSER RULES

jsong   :   exp? EOF;

args    : '(' (VAR_ID (',' VAR_ID)*)* ')';

element :   exp | range;

exp     :   '(' exp (';' exp?)* ')'                                 # block
        |   lhs = exp '~>' rhs = exp                                # chain
        |   lhs = exp '[' rhs = exp ']'                             # filter
        |   lhs = exp '.' rhs = exp                                 # map
        |   lhs = exp '.' rhs = exp (op +=(AT | HASH) VAR_ID)*      # mapAndBind
        |   lhs = exp op = (MUL | DIV | MOD) rhs = exp              # mathMULorDIVorMOD
        |   lhs = exp op = (SUM | SUB) rhs = exp                    # mathSUMorSUB
        |   lhs = exp '&' rhs = exp                                 # concatenate
        |   lhs = exp op = (LT | LE | GE | GT | NE | EQ) rhs = exp  # compare
        |   lhs = exp 'in' rhs = exp                                # include
        |   lhs = exp op = (AND | OR) rhs = exp                     # logic
        |   '{' field (',' field)* '}'                              # object
        |   '[' element (',' element)* ']'                          # array
        |   exp '[]'                                                # expand
        |   VAR_ID ':=' exp                                         # assign
        |   FUNC args '{' exp (',' exp)* '}'                        # define
        |   VAR_ID '(' (exp (',' exp)*)* ')'                        # call
        |   VAR_ID                                                  # var
        |   '/' pattern '/' 'i'                                     # regexCI
        |   '/' pattern '/' 'm'                                     # regexML
        |   '/' pattern '/'                                         # regex
        |   path                                                    # select
        |   type                                                    # literal
        ;

field   :   key = exp ':' val = exp;

type    :   STRING          # text
        |   SUB? NUMBER     # number
        |   FALSE           # false
        |   TRUE            # true
        |   NULL            # null
        ;

path    :   MOD ('.' MOD)*  # parent
        |   '$$'            # root
        |   '$'             # context
        |   '*'             # wildcard
        |   '**'            # descendants
        |   ID              # id
        ;

pattern : (~'/' | '\\' '/' '?')*;

range   : min = exp '..' max = exp;

// LEXER RULES

AT      : '@';
HASH    : '#';

LT      : '<';
LE      : '<=';
GT      : '>';
GE      : '>=';
NE      : '!=';
EQ      : '=';

DIV     : '/';
SUB     : '-';
MOD     : '%';
MUL     : '*';
SUM     : '+';

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
