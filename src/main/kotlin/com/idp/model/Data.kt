package com.idp.model

import java.time.LocalDate

sealed interface Data
data class National(val date: LocalDate, val cases: Int, val deaths: Int) : Data
data class County(
    val date: LocalDate,
    val county: String,
    val state: String,
    val fips: Int,
    val cases: Int,
    val deaths: Int
) : Data

data class State(val date: LocalDate, val state: String, val fips: Int, val cases: Int, val deaths: Int) : Data {
    constructor(vararg args: String) : this(
        date   = LocalDate.parse(args[index("date", State::class.java)]),
        state  = args[index("state", State::class.java)],
        fips   = args[index("fips", State::class.java)].toInt(),
        cases  = args[index("cases", State::class.java)].toInt(),
        deaths = args[index("deaths", State::class.java)].toInt()
    ) {
        require(args.size == this::class.java.declaredFields.size)
    }
}

fun <T : Data> index(arg: String, cl: Class<T>): Int = cl.declaredFields.map { it.name }.indexOf(arg)