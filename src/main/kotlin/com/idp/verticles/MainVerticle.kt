package com.idp.verticles

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.idp.model.*
import com.idp.parser.read
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.logger
import java.io.File

const val JSON = "application/json"
const val ALL  = -5

class MainVerticle : CoroutineVerticle() {

    private val logger = logger({}::class.java.name.takeWhile { it != '$' })
    private val nationalFile = File({}::class.java.getResource("/us.csv")?.file ?: "")
    private val stateFile    = File({}::class.java.getResource("/us-states.csv")?.file ?: "")
    private val countyFile   = File({}::class.java.getResource("/us-counties.csv")?.file ?: "")

    override suspend fun start() {
        logger.info("Starting HTTP server...")

        vertx.createHttpServer()
            .requestHandler(routes())
            .listen(8081)
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
            .handler { data<National>(it, it.pathParams()["count"]?.toIntOrNull() ?: ALL) }

        router.get("/state")
            .handler { data<State>(it, ALL) }

        router.get("/state/:count")
            .handler { data<State>(it, it.pathParams()["count"]?.toIntOrNull() ?: ALL) }

        router.get("/county")
            .handler { data<County>(it, ALL) }

        router.get("/county/:count")
            .handler { data<County>(it, it.pathParams()["count"]?.toIntOrNull() ?: ALL) }

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

    private inline fun <reified T: Data> data(rc: RoutingContext, count: Int) {
        val file = when(T::class) {
            National::class -> nationalFile
            State::class -> stateFile
            County::class -> countyFile
            else -> TODO()
        }

        val response = if (count == ALL) {
            csvReader().read(file)
        } else {
            csvReader().read<T,T>(file) { take(count) }
        }

        rc.response().setStatusCode(200)
            .putHeader("Content-Type", JSON)
            .end(response.map {it.toJson()}.toString())
    }
}