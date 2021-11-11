package com.idp

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.idp.model.County
import com.idp.parser.read
import java.io.File

@OptIn(ExperimentalStdlibApi::class)
fun main() {
    val filePath = {}::class.java.getResource("/us-counties.csv")?.file ?: ""
    csvReader().read<County, County>(File(filePath)) {
        take(10).onEach { println(it) }
    }
}
