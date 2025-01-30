package com.knifescout

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureCors()
    configureRouting()
}

fun Application.configureCors() {
    install(CORS) {
        allowHost("zealous-pond-0dbef2f03.4.azurestaticapps.net", schemes = listOf("https"))
        allowHost("localhost", schemes = listOf("http"))
        allowHost("localhost:5173", schemes = listOf("http"))
        allowMethod(HttpMethod.Get)
        allowHeaders { true }
    }
}