package com.idp.parser

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.idp.model.County
import com.idp.model.Data
import com.idp.model.National
import com.idp.model.State
import java.io.File
import java.time.LocalDate

inline fun <reified T : Data, R> CsvReader.read(file: File, crossinline seqF: Sequence<T>.() -> Sequence<R>): List<R> =
    open(file) {
        readAllWithHeaderAsSequence()
            .filter { !it.containsValue("") }
            .map { it.toData<T>() }
            .seqF()
            .toList()
    }

inline fun <reified T> Map<String, String>.toData(): T = when (T::class) {
    State::class    -> toState()
    County::class   -> toCounty()
    National::class -> toNational()
    else            -> TODO()
} as T

fun Map<String, String>.toNational(): National = National(
    date   = LocalDate.parse(get("date") ?: "1900-01-01"),
    cases  = get("cases")?.toInt() ?: -1,
    deaths = get("deaths")?.toInt() ?: -1
)

fun Map<String, String>.toState(): State = State(
    date   = LocalDate.parse(get("date") ?: "1900-01-01"),
    cases  = get("cases")?.toInt() ?: -1,
    deaths = get("deaths")?.toInt() ?: -1,
    state  = get("state") ?: "",
    fips   = get("fips")?.toInt() ?: -1
)

fun Map<String, String>.toCounty(): County = County(
    date   = LocalDate.parse(get("date") ?: "1900-01-01"),
    cases  = get("cases")?.toInt() ?: -1,
    deaths = get("deaths")?.toInt() ?: -1,
    state  = get("state") ?: "",
    fips   = get("fips")?.toInt() ?: -1,
    county = get("county") ?: ""
)
