package com.idp.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.kotlin.logger
import java.io.File
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

suspend fun updateFiles() {
    downloadFile(Properties.nationalDownload, Properties.nationalFile)
        .thenRun { Database.updateNational() }
    downloadFile(Properties.stateDownload, Properties.stateFile)
        .thenRun { Database.updateState() }
    downloadFile(Properties.countyDownload, Properties.countyFile)
        .thenRun { Database.updateCounty() }
}

private suspend fun downloadFile(source: String, destination: String) = withContext(Dispatchers.IO) {
    logger.info("Update triggered for the following file $destination")
    Runtime.getRuntime()
        .exec("curl -L $source -o $destination-backup")
        .onExit()
        .thenRun {
            File(destination).delete()
            File("$destination-backup").renameTo(File(destination))
            logger.info("Finished downloading $destination")
        }
}
