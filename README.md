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

### Declare the dependency.

The project is published in
[Maven Central](https://central.sonatype.com/artifact/io.github.lucanicoladebiasi/jsong/1.0).
comopplete of source code and documentation jars.

Declere dependency on it according the building toolchain of your choice. 

#### Gradle

```kotlin
implementation 'io.github.lucanicoladebiasi:jsong1:1.1'
```

#### Gradle in Kotlin

```kotlin
implementation("io.github.lucanicoladebiasi:jsong1:1.1")
```

#### Maven

```xml
<dependency>
    <groupId>io.github.lucanicoladebiasi</groupId>
    <artifactId>jsong</artifactId>
    <version>1.1</version>
</dependency>
```

### Code with JSong

#### Kotlin

Kotlin is handy to declare JSON objects.
Suppose  to define an `address` object as explained in the [Simple Queries](https://docs.jsonata.org/simple) page
of the official JSONata documentation: in Kotlin this is defined as follows.

```kotlin
@Language("JSON")
val root: JsonNode = mapper.readTree(
        """
        {
          "FirstName": "Fred",
          "Surname": "Smith",
          "Age": 28,
          "Address": {
            "Street": "Hursley Park",
            "City": "Winchester",
            "Postcode": "SO21 2JN"
          },
          "Phone": [
            {
              "type": "home",
              "number": "0203 544 1234"
            },
            {
              "type": "office",
              "number": "01962 001234"
            },
            {
              "type": "office",
              "number": "01962 001235"
            },
            {
              "type": "mobile",
              "number": "077 7700 1234"
            }
          ],
          "Email": [
            {
              "type": "work",
              "address": ["fred.smith@my-work.com", "fsmith@my-work.com"]
            },
            {
              "type": "home",
              "address": ["freddy@my-social.com", "frederic.smith@very-serious.com"]
            }
          ],
          "Other": {
            "Over 18 ?": true,
            "Misc": null,
            "Alternative.Address": {
              "Street": "Brick Lane",
              "City": "London",
              "Postcode": "E1 6RF"
            }
          }
        }
        """.trimIndent()
    )
```

To evaluate the JSONata expression `Address.City` to the JSON object `addreess`, the code is as simple as

```kotlin
val expression = "Address.City" 
val result = Processor(root).evaluate(expression)
```

where

* `root` is the `JsonNode` object to evaluate with the JSONata `expression`, `root` can be `null`;
* `expression` is a string being a valid JSONata statement;
* `result` returns the outcome of `expression` applied to `root`, 
   it is a `JsonNode` object and it can be `null`. 
   For the above example, `result` is a `TextNode` wrapping "Winchester" string.

JUnit tests are organized per page of the JSONata official documentation,
please, refer to them for a rich set of examples about how to use this software.

The code is documented: start reading the documentation of the `io.github.lucanicoladebiasi.jsong1.Processor` class.

## Changes from previous version

The [order-by](https://docs.jsonata.org/path-operators#---order-by) path operator is full implemented.

## Current limitations.

* Operators
  * The [transform](https://docs.jsonata.org/other-operators#-------transform) operator (planned for 1.2 release).
* Functions
  * [$formatInteger()](https://docs.jsonata.org/numeric-functions#formatinteger) doesn't implement the JS `picture` argument.
  * [$formatNumber()](https://docs.jsonata.org/numeric-functions#formatnumber) doesn't implement the `options` argument.
  * [$fromMillis()](https://docs.jsonata.org/date-time-functions#frommillis) doesn't implement the JS `picture` argument.
  * [$parseInteger()](https://docs.jsonata.org/numeric-functions#formatnumber) doesn't implement the `options` argument.
  * [$replace()](https://docs.jsonata.org/string-functions#replace) doesn't implement the `limit` argument, the `replacement` doesn't parse the regex group reference.
  * [$toMillis()](https://docs.jsonata.org/date-time-functions#tomillis) doesn't implement the JS `picture` argument.

## How to contribute

This tiny project is my attempt to return a little of what I received from the open-source community, hence feel free to
fork, criticize (nothing can offend me, I will always learn from your observations), improve, propose.

To decide what next to release, please, write an e-mail to `luca.nicola.debiasi@gmail.com` with subject line
starting with **"JSong"**: we will decide how to merge your contribution.

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


