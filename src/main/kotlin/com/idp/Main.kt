package com.idp

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.idp.model.State
import com.idp.parser.read
import java.io.File

fun main() {
    val filePath = {}::class.java.getResource("/us-counties.csv")?.file ?: ""
    val file = File(filePath)

    val stateToLength: (Sequence<State>) -> Sequence<Int> = {
        it.take(9).map(State::state).drop(2).map(String::length)
    }
    val rows = csvReader().read(file,stateToLength)

    csvReader().read<State, Int>(file) {
        take(9).drop(2).map {it.state.length}.onEach { println(it) }
    }
    println("------")
    rows.forEach { println(it) }
}

