package com.idp.model

import java.io.FileInputStream
import java.util.Properties

object Properties {
    private val properties: Properties = run {
        val path = {}::class.java.getResource("/application.properties")?.file ?: ""
        val inputStream = FileInputStream(path)
        val properties = Properties()
        properties.load(inputStream)
        properties
    }
    val port = properties.getProperty("server.port").toInt()
    val nationalFile = properties.getProperty("file.national") ?: ""
    val stateFile = properties.getProperty("file.state") ?: ""
    val countyFile = properties.getProperty("file.county") ?: ""
    val nationalDownload = properties.getProperty("download.national") ?: ""
    val stateDownload = properties.getProperty("download.state") ?: ""
    val countyDownload = properties.getProperty("download.county") ?: ""
}