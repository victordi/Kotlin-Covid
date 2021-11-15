package com.idp.parser

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.idp.model.County
import com.idp.model.Data
import com.idp.model.National
import com.idp.model.State
import java.io.File
import java.time.LocalDate

inline fun <reified T : Data> CsvReader.read(file: File): List<T> =
    open(file) {
        readAllWithHeaderAsSequence().runCatching {
            mapNotNull { it.toData<T>() }.toList()
        }.getOrDefault(emptyList())
    }

inline fun <reified T : Data, R> CsvReader.read(file: File, crossinline seqF: Sequence<T>.() -> Sequence<R>): List<R> =
    open(file) {
        readAllWithHeaderAsSequence().runCatching {
            mapNotNull { it.toData<T>() }
            .seqF()
            .toList()
        }.getOrDefault(emptyList())
    }

inline fun <reified T : Data> Map<String, String>.toData(): T? = when (T::class) {
    State::class -> toState()
    County::class -> toCounty()
    National::class -> toNational()
    else -> TODO()
} as T?

fun Map<String, String>.toNational(): National? = runCatching {
    National(
        date = LocalDate.parse(getValue("date")),
        cases = getValue("cases").toInt(),
        deaths = getValue("deaths").toInt()
    )
}.getOrNull()


fun Map<String, String>.toState(): State? = runCatching {
    State(
        date = LocalDate.parse(getValue("date")),
        cases = getValue("cases").toInt(),
        deaths = getValue("deaths").toInt(),
        state = getValue("state"),
        fips = getValue("fips").toInt()
    )
}.getOrNull()

fun Map<String, String>.toCounty(): County? = runCatching {
    County(
        date = LocalDate.parse(getValue("date")),
        cases = getValue("cases").toInt(),
        deaths = getValue("deaths").toInt(),
        state = getValue("state"),
        fips = getValue("fips").toInt(),
        county = getValue("county")
    )
}.getOrNull()
