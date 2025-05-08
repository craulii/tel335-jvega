package com.example

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int, val name: String)

fun Application.configureRouting() {
    routing {
        get("/hello") {
            call.respond(mapOf("message" to "¡Hola desde Ktor!"))
        }

        post("/users") {
            val user = call.receive<User>()
            call.respond(user)
        }

    }
}
