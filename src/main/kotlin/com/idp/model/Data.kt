package com.idp.model

import java.time.LocalDate

sealed interface Data {
    fun toJson(): String
}

data class National(val date: LocalDate, val cases: Int, val deaths: Int) : Data {
    override fun toJson(): String =
        """
        {
            "date": "$date",
            "cases": $cases,
            "deaths": $deaths
        }
    """.trimIndent()
}

data class State(val date: LocalDate, val state: String, val fips: Int, val cases: Int, val deaths: Int) : Data {
    override fun toJson(): String =
        """
        {
            "date": "$date",
            "state": "$state",
            "fips": $fips,
            "cases": $cases,
            "deaths": $deaths
        }
    """.trimIndent()
}

data class County(
    val date: LocalDate,
    val county: String,
    val state: String,
    val fips: Int,
    val cases: Int,
    val deaths: Int
) : Data {
    override fun toJson(): String =
        """
        {
            "date": "$date",
            "county": "$county",
            "state": "$state",
            "fips": $fips,
            "cases": $cases,
            "deaths": $deaths
        }
    """.trimIndent()
}