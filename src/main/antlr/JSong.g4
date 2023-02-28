/*
MIT License

Copyright (c) [2023] [Luca Nicola Debiasi]

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
grammar JSong;

@header {
    package org.jsong.antlr;
}

jsong
    : exp? EOF
    ;

arr
    : '[' exp (',' exp)* ']'
    | '[' ']'
    ;

boo
    : TRUE
    | FALSE
    ;

exp
    //| '|' loc = exp ('|' upd = exp (',' del = exp)?)? '|'               #transform
    //| '^(' sort (',' sort)* ')'                                         #orderby
    : '(' exp (';' exp)*')'                         #scope
    | lhs = exp'['  rhs = exp ']'                   #filter
    | lhs = exp '.' rhs = exp  POS lbl              #mappos
    | lhs = exp '.' rhs = exp  CTX lbl              #mapctx
    | lhs = exp '.' rhs = exp                       #map

    | lhs = exp '*' rhs = exp                       #mul
    | lhs = exp '/' rhs = exp                       #div
    | lhs = exp '%' rhs = exp                       #mod
    | lhs = exp '+' rhs = exp                       #add
    | lhs = exp '-' rhs = exp                       #sub
    | lhs = exp '&' rhs = exp                       #app
    | lhs = exp 'in' rhs = exp                      #in
    | lhs = exp '>'  rhs = exp                      #gt
    | lhs = exp '<'  rhs = exp                      #lt
    | lhs = exp '>=' rhs = exp                      #gte
    | lhs = exp '<=' rhs = exp                      #lte
    | lhs = exp '!=' rhs = exp                      #ne
    | lhs = exp '='  rhs = exp                      #eq
    | lhs = exp AND  rhs = exp                      #and
    | lhs = exp OR   rhs = exp                      #or

    | prd = exp '?' pos = exp ':' neg = exp         #ife
    | lhs = exp '~>' rhs = exp                      #chain

    | (fun  ('$' lbl)) '(' (exp (',' exp)*)? ')'    #call
    | '$' lbl ':=' exp                              #set
    | '$' lbl                                       #recall
    | fun                                           #define
    | path                                          #select
    | exp'[' ']'                                    #expand
    | '[' range (',' range)* ']'                    #ranges
    | json                                          #literal
    | REGEX                                         #regex
    ;

fun
    :  ('fun'|'function') '(' ('$' lbl (',' '$' lbl)*)? ')' '{' exp '}'
    ;

json
    : arr
    | obj
    | boo
    | nil
    | num
    | txt
    ;

lbl
    : LABEL
    ;

nil
    : NULL
    ;

num
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
    | lbl   #field
    ;

range
    : min = exp '..' max = exp
    ;

sort
    : '<' exp   #asc
    | '>' exp   #des
    | exp       #asc
    ;

txt
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
