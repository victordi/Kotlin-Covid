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

const val JSON = "application/json"
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

    private suspend fun update(rc: RoutingContext) {
        updateFiles()
        rc.response().setStatusCode(STATUS_OK)
            .end("Started the update of csv files")
    }
}
