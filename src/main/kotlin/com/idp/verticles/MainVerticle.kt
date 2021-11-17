package com.idp.verticles

import com.idp.model.State
import com.idp.model.National
import com.idp.model.County
import com.idp.model.Data
import com.idp.model.updateFiles
import com.idp.model.Database
import com.idp.model.Properties
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.logger
import java.time.LocalDate

const val JSON = "application/json"
const val HTML = "text/html; charset=utf-8"
const val ALL = Int.MAX_VALUE
const val STATUS_OK = 200
const val BAD_REQUEST = 400
const val TWELVE_HOURS = 12 * 60 * 60 * 1000L

class MainVerticle : CoroutineVerticle() {

    private val logger = logger({}::class.java.name.takeWhile { it != '$' })

    override suspend fun start() {
        vertx.setPeriodic(TWELVE_HOURS) {
            logger.info("Periodic update of the csv files")
            launch { updateFiles() }
        }

        logger.info("Starting Database")
        Database.init()
        logger.info("Database initialized")

        logger.info("Starting HTTP server...")
        vertx.createHttpServer()
            .requestHandler(routes())
            .listen(Properties.port)
            .onComplete { logger.info("HTTP server started at localhost:${it.result().actualPort()}") }
            .await()
    }

    override suspend fun stop() {
        super.stop()
    }

    private fun routes(): Router {
        val router = Router.router(vertx)

        router.get("/national")
            .handler { data<National>(it, ALL) }

        router.get("/national/filters")
            .handler { nationalFilters(it) }

        router.get("/national/:count")
            .handler { data<National>(it, it.pathParams()["count"]?.toIntOrNull()) }

        router.get("/state")
            .handler { data<State>(it, ALL) }

        router.get("/state/filters")
            .handler { stateFilters(it) }

        router.get("/state/:count")
            .handler { data<State>(it, it.pathParams()["count"]?.toIntOrNull()) }

        router.get("/county")
            .handler { data<County>(it, ALL) }

        router.get("/county/filters")
            .handler { countyFilters(it) }

        router.get("/county/:count")
            .handler { data<County>(it, it.pathParams()["count"]?.toIntOrNull()) }

        router.get("/info/:name")
            .handler { info(it) }

        router.get("/graph/:name")
            .handler { graph(it) }

        router.get("/update")
            .coroutineHandler { update(it) }

        return router
    }

    private fun Route.coroutineHandler(handler: suspend (RoutingContext) -> Unit) = handler {
        launch(it.vertx().dispatcher()) {
            try {
                handler(it)
            } catch (e: Exception) {
                it.fail(e)
            }
        }
    }

    private fun nationalFilters(rc: RoutingContext) = runCatching {
        val queryParams = rc.queryParams()
        require(!queryParams.isEmpty)
        val startDate = LocalDate.parse(queryParams["startDate"] ?: LocalDate.MIN.toString())
        val endDate = LocalDate.parse(queryParams["endDate"] ?: LocalDate.MAX.toString())

        val response = Database.nationalSequence
            .filter { it.date >= startDate && it.date <= endDate }
            .toList()

        rc.response().setStatusCode(STATUS_OK)
            .putHeader("Content-Type", JSON)
            .end(response.map { it.toJson() }.toString())
    }.onFailure {
        rc.response().setStatusCode(BAD_REQUEST)
            .end("At least one filter must be provided")
    }

    private fun stateFilters(rc: RoutingContext) = runCatching {
        val queryParams = rc.queryParams()
        require(!queryParams.isEmpty)
        val startDate = LocalDate.parse(queryParams["startDate"] ?: LocalDate.MIN.toString())
        val endDate = LocalDate.parse(queryParams["endDate"] ?: LocalDate.MAX.toString())
        val state = queryParams["state"] ?: ""

        val response = Database.stateSequence
            .filter { it.date >= startDate && it.date <= endDate }
            .filter { state == "" || it.state == state }
            .toList()

        rc.response().setStatusCode(STATUS_OK)
            .putHeader("Content-Type", JSON)
            .end(response.map { it.toJson() }.toString())
    }.onFailure {
        rc.response().setStatusCode(BAD_REQUEST)
            .end("At least one filter must be provided")
    }

    private fun countyFilters(rc: RoutingContext) = runCatching {
        val queryParams = rc.queryParams()
        require(!queryParams.isEmpty)
        val startDate = LocalDate.parse(queryParams["startDate"] ?: LocalDate.MIN.toString())
        val endDate = LocalDate.parse(queryParams["endDate"] ?: LocalDate.MAX.toString())
        val county = queryParams["county"] ?: ""
        val state = queryParams["state"] ?: ""

        val response = Database.countySequence
            .filter { it.date >= startDate && it.date <= endDate }
            .filter { county == "" || it.county == county }
            .filter { state == "" || it.state == state }
            .toList()

        rc.response().setStatusCode(STATUS_OK)
            .putHeader("Content-Type", JSON)
            .end(response.map { it.toJson() }.toString())
    }.onFailure {
        rc.response().setStatusCode(BAD_REQUEST)
            .end("At least one filter must be provided")
    }

    private inline fun <reified T : Data> data(rc: RoutingContext, count: Int?) = runCatching {
        requireNotNull(count)
        val inputStream = when (T::class) {
            National::class -> Database.nationalSequence
            State::class -> Database.stateSequence
            County::class -> Database.countySequence
            else -> emptySequence()
        }

        val response = inputStream.take(count).toList()

        rc.response().setStatusCode(STATUS_OK)
            .putHeader("Content-Type", JSON)
            .end(response.map { it.toJson() }.toString())
    }.onFailure {
        rc.response().setStatusCode(BAD_REQUEST)
            .end("Provided path parameter is not an valid Integer value")
    }

    private fun info(rc: RoutingContext) = runCatching {
        val name = rc.pathParams()["name"]
        requireNotNull(name)
        val response = when(name) {
            "national" -> Database.nationalInfo()
            in Database.states -> Database.stateInfo(name)
            in Database.counties -> Database.countyInfo(name)
            else -> throw IllegalArgumentException("Provided path parameter is not a state or a county")
        }
        rc.response().setStatusCode(STATUS_OK)
            .putHeader("Content-Type", JSON)
            .end(response)
    }.onFailure {
        rc.response().setStatusCode(BAD_REQUEST)
            .end(it.message)
    }

    private fun graph(rc: RoutingContext) = runCatching {
        val name = rc.pathParams()["name"]
        requireNotNull(name)
        val graphPoints = when(name) {
            "national" -> Database.nationalGraph()
            in Database.states -> Database.stateGraph(name)
            in Database.counties -> Database.countyGraph(name)
            else -> throw IllegalArgumentException("Provided path parameter is not a state or a county")
        }

        val response = {}::class.java.getResource("/graph.txt")!!.readText()
            .replace("data-placeholder", graphPoints)

        rc.response().setStatusCode(STATUS_OK)
            .putHeader("Content-Type", HTML)
            .end(response)
    }.onFailure {
        rc.response().setStatusCode(BAD_REQUEST)
            .end(it.message)
    }

    private suspend fun update(rc: RoutingContext) {
        updateFiles()
        rc.response().setStatusCode(STATUS_OK)
            .end("Started the update of csv files")
    }
}
