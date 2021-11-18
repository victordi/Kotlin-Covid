package com.idp.model

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.idp.parser.read
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.kotlin.logger
import java.io.File

object Database {
    private val logger = logger({}::class.java.name.takeWhile { it != '$' })

    lateinit var nationalSequence: Sequence<National>
        private set
    lateinit var stateSequence: Sequence<State>
        private set
    lateinit var countySequence: Sequence<County>
        private set
    lateinit var states: Set<String>
        private set
    lateinit var counties: Set<String>
        private set

    private const val DAY = 1
    private const val WEEK = 7
    private const val MONTH = 30
    private const val YEAR = 365

    suspend fun init() = withContext(Dispatchers.IO) {
        nationalSequence =
            csvReader().read<National>(File(Properties.nationalFile)).asSequence().sortedByDescending { it.date }
        stateSequence =
            csvReader().read<State>(File(Properties.stateFile)).asSequence().sortedByDescending { it.date }
        countySequence =
            csvReader().read<County>(File(Properties.countyFile)).asSequence().sortedByDescending { it.date }

        states = stateSequence.map { it.state }.toSet()
        counties = countySequence.map { it.county }.toSet()
    }

    fun updateNational() {
        logger.info("An update for the National database was triggered")

        val backup =
            csvReader().read<National>(File(Properties.nationalFile)).asSequence().sortedByDescending { it.date }
        nationalSequence = backup

        logger.info("Database update finished for National File")
    }

    fun updateState() {
        logger.info("An update for the State database was triggered")

        val backup =
            csvReader().read<State>(File(Properties.stateFile)).asSequence().sortedByDescending { it.date }
        stateSequence = backup

        logger.info("Database update finished for State File")
    }

    fun updateCounty() {
        logger.info("An update for the County database was triggered")

        val backup =
            csvReader().read<County>(File(Properties.countyFile)).asSequence().sortedByDescending { it.date }
        countySequence = backup

        logger.info("Database update finished for County File")
    }

    fun nationalInfo(): String = getInfo(nationalSequence)

    fun stateInfo(state: String): String = getInfo(stateSequence.filter { it.state == state })

    fun countyInfo(county: String): String = getInfo(countySequence.filter { it.county == county })

    private fun getInfo(sequence: Sequence<Data>): String {
        operator fun Data.minus(other: Data): Pair<Int, Int> =
            this.cases - other.cases to this.deaths - other.deaths

        val today = sequence.first()
        val lastDay: Pair<Int, Int> = today - sequence.drop(DAY).first()
        val lastWeek: Pair<Int, Int> = today - sequence.drop(WEEK).first()
        val lastMonth: Pair<Int, Int> = today - sequence.drop(MONTH).first()
        val lastYear: Pair<Int, Int> = today - sequence.drop(YEAR).first()

        return """
            {
                "cases": {
                    "yesterday": ${lastDay.first},
                    "lastWeek": ${lastWeek.first},
                    "lastMonth": ${lastMonth.first},
                    "lastYear": ${lastYear.first}
                },
                "deaths": {
                    "yesterday":${lastDay.second},
                    "lastWeek":${lastWeek.second},
                    "lastMonth":${lastMonth.second},
                    "lastYear": ${lastYear.second}
                }
            }
        """.trimIndent()
    }

    fun nationalGraph(): String = getWeeklyGraph(nationalSequence)

    fun stateGraph(state: String): String = getWeeklyGraph(stateSequence.filter { it.state == state })

    fun countyGraph(county: String): String = getWeeklyGraph(countySequence.filter { it.county == county })

    private fun getWeeklyGraph(sequence: Sequence<Data>): String {
        val weeklySequence = sequence.filterIndexed { index, _ -> index % WEEK == 0 }
        val dates = weeklySequence.map { it.date.toEpochDay() }.drop(1)
        val percentages = weeklySequence.map { it.cases }.zipWithNext { a, b -> (a - b) / b.toDouble() }

        fun List<Pair<Long, Double>>.asString(): String =
            fold("") { acc, (date, percentage) -> "$acc[$date,  $percentage],\n" }

        return dates.zip(percentages).toList().asString()
    }

}
