package com.idp.model

import org.apache.logging.log4j.kotlin.logger
import java.time.LocalDate

sealed interface Data {
    val date: LocalDate
    val cases: Int
    val deaths: Int

    fun toJson(): String
}

data class National(override val date: LocalDate, override val cases: Int, override val deaths: Int) : Data {
    override fun toJson(): String =
        """
        {
            "date": "$date",
            "cases": $cases,
            "deaths": $deaths
        }
    """.trimIndent()
}

data class State(
    override val date: LocalDate,
    val state: String,
    val fips: Int,
    override val cases: Int,
    override val deaths: Int
) :
    Data {
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
    override val date: LocalDate,
    val county: String,
    val state: String,
    val fips: Int,
    override val cases: Int,
    override val deaths: Int
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

private val logger = logger({}::class.java.name.takeWhile { it != '$' })
