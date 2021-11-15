package com.idp.parser

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.idp.model.County
import com.idp.model.National
import com.idp.model.State
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.io.File

class CsvParserTest: FreeSpec({
    "national" - {
        val file = File("national")
        "should parse csv to National correctly" {
            file.createNewFile()
            file.writeText("date,cases,deaths\n")
            file.appendText("2020-01-21,1,0\n")
            file.appendText("2020-01-22,2,1\n")
            file.appendText("2020-01-23,3,2\n")

            val result: List<National> = csvReader().read<National, National>(file) {
                drop(1)
            }

            result[0].cases shouldBe 2
            result[1].cases shouldBe 3

            file.delete()
        }

        "should result in empty list if wrong format" {
            file.createNewFile()
            file.writeText("date,cases,wrong\n")
            file.appendText("2020-01-21,1,0\n")

            val result: List<National> = csvReader().read(file)

            result shouldBe emptyList()

            file.delete()
        }
    }

    "state" - {
        val file = File("state")
        "should parse csv to State correctly" {
            file.createNewFile()
            file.writeText("date,state,fips,cases,deaths\n")
            file.appendText("2020-01-21,Washington,53,1,0\n")
            file.appendText("2020-01-22,Washington,53,2,1\n")
            file.appendText("2020-01-22,Illinois,17,1,1\n")

            val result: List<State> = csvReader().read<State, State>(file) {
                drop(1)
            }

            result[0].cases shouldBe 2
            result[0].state shouldBe "Washington"
            result[1].cases shouldBe 1
            result[1].state shouldBe "Illinois"

            file.delete()
        }

        "should result in empty list if wrong format" {
            file.createNewFile()
            file.writeText("date,state,fips,cases,deaths\n")
            file.appendText("2020-01-21,Washington,53,1,wrong\n")

            val result: List<National> = csvReader().read(file)

            result shouldBe emptyList()

            file.delete()
        }
    }

    "county" - {
        val file = File("county")
        "should parse csv to County correctly" {
            file.createNewFile()
            file.writeText("date,county,state,fips,cases,deaths\n")
            file.appendText("2020-01-21,Snohomish,Washington,53061,1,0\n")
            file.appendText("2020-01-26,Los Angeles,California,06037,1,0\n")
            file.appendText("2020-01-28,Maricopa,Arizona,04013,1,0\n")

            val result: List<County> = csvReader().read<County, County>(file) {
                take(2)
            }

            result[0].cases shouldBe 1
            result[0].county shouldBe "Snohomish"
            result[1].cases shouldBe 1
            result[1].county shouldBe "Los Angeles"

            file.delete()
        }

        "should result in empty list if wrong format" {
            file.createNewFile()
            file.writeText("date,county,state,fips,cases,deaths\n")
            file.appendText("Invalid Date,Snohomish,Washington,53061,1,0\n")

            val result: List<National> = csvReader().read(file)

            result shouldBe emptyList()

            file.delete()
        }
    }
})
