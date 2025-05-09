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

@Serializable
data class Origen(val pais: String)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Libro(val id: Int, val titulo: String, val autor: String, val origen: Origen)

@Serializable
data class LibroInput(val titulo: String, val autor: String, val pais: String)

val libros = mutableListOf(
    Libro(id = 1, titulo = "Cien años de soledad", autor = "Gabriel García Márquez", origen = Origen("Colombia")),
    Libro(id = 2, titulo = "El amor en los tiempos del cólera", autor = "Gabriel García Márquez", origen = Origen("Colombia")),
    Libro(id = 3, titulo = "Don Quijote de la Mancha", autor = "Miguel de Cervantes", origen = Origen("España")),
    Libro(id = 4, titulo = "La Galatea", autor = "Miguel de Cervantes", origen = Origen("España")),
    Libro(id = 5, titulo = "1984", autor = "George Orwell", origen = Origen("Reino Unido")),
    Libro(id = 6, titulo = "Rebelión en la granja", autor = "George Orwell", origen = Origen("Reino Unido")),
    Libro(id = 7, titulo = "El principito", autor = "Antoine de Saint-Exupéry", origen = Origen("Francia")),
    Libro(id = 8, titulo = "Correo del Sur", autor = "Antoine de Saint-Exupéry", origen = Origen("Francia")),
    Libro(id = 9, titulo = "Orgullo y prejuicio", autor = "Jane Austen", origen = Origen("Reino Unido")),
    Libro(id = 10, titulo = "Emma", autor = "Jane Austen", origen = Origen("Reino Unido")),
    Libro(id = 11, titulo = "Kamasutra", autor = "Vatsyayana", origen = Origen("India")),
    Libro(id = 12, titulo = "Bhagavad Gita", autor = "Vyasa", origen = Origen("India")),
    Libro(id = 13, titulo = "The Book of the Law", autor = "Aleister Crowley", origen = Origen("Reino Unido")),
    Libro(id = 14, titulo = "Magick in Theory and Practice", autor = "Aleister Crowley", origen = Origen("Reino Unido")),
    Libro(id = 15, titulo = "The Vision and the Voice", autor = "Aleister Crowley", origen = Origen("Reino Unido")),
    Libro(id = 16, titulo = "Diary of a Drug Fiend", autor = "Aleister Crowley", origen = Origen("Reino Unido")),
    Libro(id = 17, titulo = "El Diablo", autor = "Antonio García", origen = Origen("México")),
    Libro(id = 18, titulo = "Rituales oscuros", autor = "Lucía Herrera", origen = Origen("México"))
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

        get("/libros") {
            val paisFiltro = call.request.queryParameters["pais"]
            val librosFiltrados = if (paisFiltro != null) {
                libros.filter { it.origen.pais.equals(paisFiltro, ignoreCase = true) }
            } else {
                libros
            }

            call.respond(librosFiltrados)
        }

        get("/libros/pais/{pais}") {
            val pais = call.parameters["pais"]
            if (pais != null) {
                val librosPorPais = libros.filter { it.origen.pais.equals(pais, ignoreCase = true) }
                call.respond(librosPorPais)
            } else {
                call.respond(HttpStatusCode.BadRequest, "País no especificado")
            }
        }

        get("/libros/autor/{nombre}") {
            val nombre = call.parameters["nombre"]
            if (nombre != null) {
                val librosPorAutor = libros.filter { it.autor.contains(nombre, ignoreCase = true) }
                call.respond(librosPorAutor)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Autor no especificado")
            }
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
            val nuevoLibro = Libro(id = currentId++, titulo = libroInput.titulo, autor = libroInput.autor, origen = Origen(libroInput.pais))
            libros.add(nuevoLibro)

            call.respond(HttpStatusCode.Created, nuevoLibro)
        }

        put("/libros/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val libroExistente = libros.find { it.id == id }
                if (libroExistente != null) {
                    val libroInput = call.receive<LibroInput>()
                    val libroActualizado = Libro(id = id, titulo = libroInput.titulo, autor = libroInput.autor, origen = Origen(libroInput.pais))
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
