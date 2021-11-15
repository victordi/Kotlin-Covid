package com.idp.verticles

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.idp.model.National
import com.idp.model.County
import com.idp.model.State
import com.idp.model.Data
import com.idp.parser.read
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.kotlin.logger
import java.io.File
import java.io.FileInputStream
import java.util.Properties

const val JSON = "application/json"
const val ALL = Int.MAX_VALUE
const val STATUS_OK = 200
const val BAD_REQUEST = 400
const val TWELVE_HOURS = 12 * 60 * 60 * 1000L

class MainVerticle : CoroutineVerticle() {

    private val logger = logger({}::class.java.name.takeWhile { it != '$' })
    private val properties: Properties = run {
        val path = {}::class.java.getResource("/application.properties")?.file ?: ""
        val inputStream = FileInputStream(path)
        val properties = Properties()
        properties.load(inputStream)
        properties
    }
    private val port = properties.getProperty("server.port").toInt()
    private val nationalFile = properties.getProperty("file.national") ?: ""
    private val stateFile = properties.getProperty("file.state") ?: ""
    private val countyFile = properties.getProperty("file.county") ?: ""
    private val nationalDownload = properties.getProperty("download.national") ?: ""
    private val stateDownload = properties.getProperty("download.state") ?: ""
    private val countyDownload = properties.getProperty("download.county") ?: ""

    override suspend fun start() {
        vertx.setPeriodic(TWELVE_HOURS) {
            logger.info("Updating csv files")
            launch { updateFiles() }
        }

        logger.info("Starting HTTP server...")
        vertx.createHttpServer()
            .requestHandler(routes())
            .listen(port)
            .onComplete { logger.info("HTTP server started at localhost:${it.result().actualPort()}") }
            .await()
    }

    override suspend fun stop() {
        super.stop()
    }

    private suspend fun updateFiles() {
        downloadFile(nationalDownload, nationalFile)
        downloadFile(stateDownload, stateFile)
        downloadFile(countyDownload, countyFile)
    }

    private suspend fun downloadFile(source: String, destination: String) = withContext(Dispatchers.IO) {
        Runtime.getRuntime()
            .exec("curl -L $source -o $destination-backup")
            .onExit()
            .thenRun {
                File(destination).delete()
                File("$destination-backup").renameTo(File(destination))
                logger.info("Finished downloading $destination")
            }
    }

    private fun routes(): Router {
        val router = Router.router(vertx)

        router.get("/national")
            .handler { data<National>(it, ALL) }

        router.get("/national/:count")
            .handler { data<National>(it, it.pathParams()["count"]?.toIntOrNull()) }

        router.get("/state")
            .handler { data<State>(it, ALL) }

        router.get("/state/:count")
            .handler { data<State>(it, it.pathParams()["count"]?.toIntOrNull()) }

        router.get("/county")
            .handler { data<County>(it, ALL) }

        router.get("/county/:count")
            .handler { data<County>(it, it.pathParams()["count"]?.toIntOrNull()) }

        return router
    }

    private inline fun <reified T : Data> data(rc: RoutingContext, count: Int?) = runCatching {
        requireNotNull(count)
        val file = File(
            when (T::class) {
                National::class -> nationalFile
                State::class -> stateFile
                County::class -> countyFile
                else -> TODO()
            }
        )

        val response = csvReader().read<T, T>(file) { take(count) }

        rc.response().setStatusCode(STATUS_OK)
            .putHeader("Content-Type", JSON)
            .end(response.map { it.toJson() }.toString())
    }.onFailure {
        rc.response().setStatusCode(BAD_REQUEST)
            .end("Provided path parameter is not an valid Integer value")
    }
}
