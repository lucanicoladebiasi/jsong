#   JSong

### JSONata On New Ground!

## Why yet another implementation for JSONata?

This project provides an Open Source implementation of the language
[JSONata 2](https://jsonata.org)
created by Andrew Coleman.

Promising to keep the compatibility with the JSONata 2 grammar,
this project extends the grammar aiming to provide an useful language for 
[ETL](https://en.wikipedia.org/wiki/Extract,_transform,_load)
operations and workflow definition.

This implementation was created purely from the JSONata language specifications
without any debt from any previous implementation, notably the 
[JavaScript JSONata](https://github.com/jsonata-js/jsonata)
project and
[IBM JSONata4Java](https://github.com/IBM/JSONata4Java)
project to allow the most permissive use of the code.

The author of the project strongly believes

> It is through art, and through art only, that we can realise our perfection. (Oscar Wilde)

without being obsessed if the *perfection* quoted is our or not, just happy to be in search of it,
returning to community of developers a drop of what he withdrew in many years of career.
Art is a gift, a gift should be free from the reciprocation, whatever
[Claude_Lévi-Strauss](https://en.wikipedia.org/wiki/Claude_L%C3%A9vi-Strauss)
wrote about, albeit about different contexts.

This project uses
[ANTLR 4](https://www.antlr.org/)
to define the JSONata grammar and to generate its parser.
The language processor is implemented in the
[Kotlin 1.8](https://kotlinlang.org/)
language.

## How to use.

### Declere the dependency

The project is published in
[Maven Central](https://central.sonatype.com/artifact/io.github.lucanicoladebiasi/jsong/1.0).
comopplete of source code and documentation jars.

Declere dependency on it according the building toolchain of your choice. 

#### Gradle

```kotlin
implementation 'io.github.lucanicoladebiasi:jsong:1.0'
```

#### Gradle in Kotlin

```kotlin
implementation("io.github.lucanicoladebiasi:jsong:1.0")
```

#### Maven

```xml
<dependency>
    <groupId>io.github.lucanicoladebiasi</groupId>
    <artifactId>jsong</artifactId>
    <version>1.0</version>
</dependency>
```

## Current limitations.

## How to contribute

##  MIT License

Copyright (c) 2023 Luca Nicola Debiasi

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


