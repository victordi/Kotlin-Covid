package com.idp.parser

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.idp.model.County
import com.idp.model.Data
import com.idp.model.National
import com.idp.model.State
import java.io.File
import java.lang.NumberFormatException
import java.time.LocalDate
import java.time.format.DateTimeParseException

inline fun <reified T : Data, R> CsvReader.read(file: File, crossinline seqF: Sequence<T>.() -> Sequence<R>): List<R> =
    open(file) {
        readAllWithHeaderAsSequence()
            .mapNotNull { it.toData<T>() }
            .seqF()
            .toList()
    }

inline fun <reified T> Map<String, String>.toData(): T? = when (T::class) {
    State::class    -> toState()
    County::class   -> toCounty()
    National::class -> toNational()
    else            -> TODO()
} as T?

fun Map<String, String>.toNational(): National? = try {
    National(
        date   = LocalDate.parse(getValue("date")),
        cases  = getValue("cases").toInt(),
        deaths = getValue("deaths").toInt()
    )
} catch (e: NumberFormatException) {
    null
} catch (e: DateTimeParseException) {
    null
} catch (e: NoSuchElementException) {
    null
}

fun Map<String, String>.toState(): State? = try {
    State(
        date   = LocalDate.parse(getValue("date")),
        cases  = getValue("cases").toInt(),
        deaths = getValue("deaths").toInt(),
        state  = getValue("state"),
        fips   = getValue("fips").toInt()
    )
} catch (e: NumberFormatException) {
    null
} catch (e: DateTimeParseException) {
    null
} catch (e: NoSuchElementException) {
    null
}

fun Map<String, String>.toCounty(): County? = try {
    County(
        date   = LocalDate.parse(getValue("date")),
        cases  = getValue("cases").toInt(),
        deaths = getValue("deaths").toInt(),
        state  = getValue("state"),
        fips   = getValue("fips").toInt(),
        county = getValue("county")
    )
} catch (e: NumberFormatException) {
    null
} catch (e: DateTimeParseException) {
    null
} catch (e: NoSuchElementException) {
    null
}