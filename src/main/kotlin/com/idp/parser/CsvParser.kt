package com.idp.parser

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.idp.model.County
import com.idp.model.Data
import com.idp.model.National
import com.idp.model.State
import java.io.File
import java.time.LocalDate

/*
 * seqF lambda must only apply non-terminal sequence steps
 */
inline fun <reified T : Data, R> CsvReader.read(file: File, crossinline seqF: Sequence<T>.() -> Sequence<R>): List<R> {
    val header = open(file) { readAllAsSequence().first() }

    val mapper: (List<String>) -> T = when (T::class) {
        State::class -> { line -> line.toState(header) as T }
        County::class -> { line -> line.toCounty(header) as T }
        National::class -> { line -> line.toNational(header) as T }
        else -> TODO() /* This can never be reached, when should be exhaustive */
    }

    return open(file) {
        readAllAsSequence()
            .drop(1)
            .filter { !it.contains("") }
            .map(mapper)
            .seqF()
            .toList()
    }
}

fun List<String>.toNational(header: List<String>): National = National(
    date = LocalDate.parse(this[header.indexOf("date")]),
    cases = this[header.indexOf("cases")].toInt(),
    deaths = this[header.indexOf("deaths")].toInt()
)

fun List<String>.toState(header: List<String>): State = State(
    date = LocalDate.parse(this[header.indexOf("date")]),
    cases = this[header.indexOf("cases")].toInt(),
    deaths = this[header.indexOf("deaths")].toInt(),
    state = this[header.indexOf("state")],
    fips = this[header.indexOf("fips")].toInt()
)


fun List<String>.toCounty(header: List<String>): County = County(
    date = LocalDate.parse(this[header.indexOf("date")]),
    cases = this[header.indexOf("cases")].toInt(),
    deaths = this[header.indexOf("deaths")].toInt(),
    state = this[header.indexOf("state")],
    fips = this[header.indexOf("fips")].toInt(),
    county = this[header.indexOf("county")]
)