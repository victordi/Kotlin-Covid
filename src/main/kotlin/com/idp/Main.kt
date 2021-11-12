package com.idp

import com.idp.verticles.MainVerticle
import io.vertx.core.Vertx

fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(MainVerticle())
}
