package com.example

import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import io.ktor.http.HttpStatusCode

// Entidad Libro
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Libro(val id: Int, val titulo: String, val autor: String)
@Serializable
data class LibroInput(val titulo: String, val autor: String)


// Repositorio en memoria con datos de ejemplo
val libros = mutableListOf(
    Libro(id = 1, titulo = "Cien años de soledad", autor = "Gabriel García Márquez"),
    Libro(id = 2, titulo = "Don Quijote de la Mancha", autor = "Miguel de Cervantes"),
    Libro(id = 3, titulo = "1984", autor = "George Orwell"),
    Libro(id = 4, titulo = "El principito", autor = "Antoine de Saint-Exupéry"),
    Libro(id = 5, titulo = "Orgullo y prejuicio", autor = "Jane Austen"),
    Libro(id = 6, titulo = "Kamasutra", autor = "Wuelmer Saldaña")

)
var currentId = 6

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        get("/") {
            call.respondText("¡Servidor en funcionamiento!", ContentType.Text.Plain)
        }

        // CRUD de libros
        get("/libros") {
            call.respond(libros)
        }

        get("/libros/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val libro = libros.find { it.id == id }
                if (libro != null) {
                    call.respond(libro)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Libro no encontrado")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "ID inválido")
            }
        }

        post("/libros") {
            val libroInput = call.receive<LibroInput>()
            val nuevoLibro = Libro(id = currentId++, titulo = libroInput.titulo, autor = libroInput.autor)
            libros.add(nuevoLibro)
            call.respond(HttpStatusCode.Created, nuevoLibro)
        }


        put("/libros/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val libroExistente = libros.find { it.id == id }
                if (libroExistente != null) {
                    val libroInput = call.receive<LibroInput>()
                    val libroActualizado = Libro(id = id, titulo = libroInput.titulo, autor = libroInput.autor)
                    libros[libros.indexOf(libroExistente)] = libroActualizado
                    call.respond(libroActualizado)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Libro no encontrado")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "ID inválido")
            }
        }


        delete("/libros/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val libro = libros.find { it.id == id }
                if (libro != null) {
                    libros.remove(libro)
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Libro no encontrado")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "ID inválido")
            }
        }
    }
}
