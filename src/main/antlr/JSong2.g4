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

exp_to_eof
    :   exp* EOF
    ;

exp
    :   ID                              # id
    |   WILD                            # field_values
    |   DESC                            # descendants
    |   ROOT                            # path_root
    |   (PRC | (DOT PRC)+)              # parent
    |   DOT ID                          # path
    |   ARR_L exp ARR_R                 # array
    |   MINUS exp                       # negative
    |   PAR_L (exp (SC (exp)?)*)? PAR_R # block
    |   NUMBER                          # number
    ;

ARR_L: '[';
ARR_R: ']';

DESC: '**';

DOT: '.';

ID
	:   [\p{L}] [\p{L}0-9_]*
	|   BACK_QUOTE ~[`]* BACK_QUOTE
	;

MINUS: '-';

NUMBER
    :   INT '.' [0-9]+ EXP? // 1.35, 1.35E-9, 0.3
    |   INT EXP             // 1e10 3e4
    |   INT                 // 3, 45
    ;

PAR_L: '(';
PAR_R: ')';

PRC: '%';

ROOT: '$$';

SC: ';';

WILD: '*';

fragment BACK_QUOTE: '`';

fragment INT : '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP : [Ee] [+\-]? INT ;    // \- since - means "range" inside [...]
