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
        allowHost("franzvrolijk.github.io", schemes = listOf("https"))
        allowMethod(HttpMethod.Get)
        allowHeaders { true }
    }
}