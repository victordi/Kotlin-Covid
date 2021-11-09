package com.idp.parser

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.idp.model.County
import com.idp.model.National
import com.idp.model.State
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.io.File

class CsvParserTest: FreeSpec({
    "should parse csv to National correctly" {
        File("test").createNewFile()
        File("test").writeText("date,cases,deaths\n")
        File("test").appendText("2020-01-21,1,0\n")
        File("test").appendText("2020-01-22,2,1\n")
        File("test").appendText("2020-01-23,3,2\n")
        val result: List<National> = csvReader().read<National,National>(File("test")) {
            drop(1)
        }
        result[0].cases shouldBe 2
        result[1].cases shouldBe 3

        File("test").delete()
    }

    "should parse csv to State correctly" {
        File("test").createNewFile()
        File("test").writeText("date,state,fips,cases,deaths\n")
        File("test").appendText("2020-01-21,Washington,53,1,0\n")
        File("test").appendText("2020-01-22,Washington,53,2,1\n")
        File("test").appendText("2020-01-22,Illinois,17,1,1\n")
        val result: List<State> = csvReader().read<State,State>(File("test")) {
            drop(1)
        }
        result[0].cases shouldBe 2
        result[0].state shouldBe "Washington"
        result[1].cases shouldBe 1
        result[1].state shouldBe "Illinois"

        File("test").delete()
    }

    "should parse csv to County correctly" {
        File("test").createNewFile()
        File("test").writeText("date,county,state,fips,cases,deaths\n")
        File("test").appendText("2020-01-21,Snohomish,Washington,53061,1,0\n")
        File("test").appendText("2020-01-26,Los Angeles,California,06037,1,0\n")
        File("test").appendText("2020-01-28,Maricopa,Arizona,04013,1,0\n")
        val result: List<County> = csvReader().read<County,County>(File("test")) {
            take(2)
        }
        result[0].cases shouldBe 1
        result[0].county shouldBe "Snohomish"
        result[1].cases shouldBe 1
        result[1].county shouldBe "Los Angeles"

        File("test").delete()
    }

})