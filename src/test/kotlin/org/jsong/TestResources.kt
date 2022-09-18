package org.jsong

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.intellij.lang.annotations.Language

object TestResources {

    val mapper = ObjectMapper()

    @Language("JSON")
    val address: JsonNode = mapper.readTree(
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

    @Language("JSON")
    val array = mapper.readTree(
        """
        [
          { "ref": [ 1,2 ] },
          { "ref": [ 3,4 ] }
        ]
        """.trimIndent()
    )

    @Language("JSON")
    val items = mapper.readTree(
        """
        {
          "Items": [
            { "Number":  1},
            { "Number":  2},
            { "Number":  3}
          ]   
        }
    """.trimIndent()
    )

    @Language("JSON")
    val library = mapper.readTree(
        """
        {
          "library": {
            "books": [
              {
                "title": "Structure and Interpretation of Computer Programs",
                "authors": [
                  "Abelson",
                  "Sussman"
                ],
                "isbn": "9780262510875",
                "price": 38.9,
                "copies": 2
              },
              {
                "title": "The C Programming Language",
                "authors": [
                  "Kernighan",
                  "Richie"
                ],
                "isbn": "9780131103627",
                "price": 33.59,
                "copies": 3
              },
              {
                "title": "The AWK Programming Language",
                "authors": [
                  "Aho",
                  "Kernighan",
                  "Weinberger"
                ],
                "isbn": "9780201079814",
                "copies": 1
              },
              {
                "title": "Compilers: Principles, Techniques, and Tools",
                "authors": [
                  "Aho",
                  "Lam",
                  "Sethi",
                  "Ullman"
                ],
                "isbn": "9780201100884",
                "price": 23.38,
                "copies": 1
              }
            ],
            "loans": [
              {
                "customer": "10001",
                "isbn": "9780262510875",
                "return": "2016-12-05"
              },
              {
                "customer": "10003",
                "isbn": "9780201100884",
                "return": "2016-10-22"
              },
              {
                "customer": "10003",
                "isbn": "9780262510875",
                "return": "2016-12-22"
              }
            ],
            "customers": [
              {
                "id": "10001",
                "name": "Joe Doe",
                "address": {
                  "street": "2 Long Road",
                  "city": "Winchester",
                  "postcode": "SO22 5PU"
                }
              },
              {
                "id": "10002",
                "name": "Fred Bloggs",
                "address": {
                  "street": "56 Letsby Avenue",
                  "city": "Winchester",
                  "postcode": "SO22 4WD"
                }
              },
              {
                "id": "10003",
                "name": "Jason Arthur",
                "address": {
                  "street": "1 Preddy Gate",
                  "city": "Southampton",
                  "postcode": "SO14 0MG"
                }
              }
            ]
          }
        }
    """.trimIndent())
}