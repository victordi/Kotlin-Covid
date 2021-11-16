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

    suspend fun init() = withContext(Dispatchers.IO) {
        nationalSequence =
            csvReader().read<National>(File(Properties.nationalFile)).asSequence().sortedByDescending { it.date }
        stateSequence =
            csvReader().read<State>(File(Properties.stateFile)).asSequence().sortedByDescending { it.date }
        countySequence =
            csvReader().read<County>(File(Properties.countyFile)).asSequence().sortedByDescending { it.date }
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
}
