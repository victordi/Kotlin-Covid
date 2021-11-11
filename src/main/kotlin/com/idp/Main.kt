package com.idp

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.idp.model.State
import com.idp.parser.read
import com.idp.parser.read2
import java.io.File

fun main() {
    val filePath = {}::class.java.getResource("/us-counties.csv")?.file ?: ""

    csvReader().read2<State, Int>(File(filePath)) {
        take(9).drop(2).map {it.state.length}.onEach { println(it) }
    }
}
