import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.*

data class Snippet(val text: String)

val snippets = Collections.synchronizedList(
    mutableListOf(
        Snippet("Hello"),
        Snippet("KTOR!")
    )
)

fun Application.module() {
    install(ContentNegotiation) {
        jackson { }
    }
    routing {
        get("/") {
            call.respondText("My Example Blog", ContentType.Text.Html)
        }
        get("/status") {
            call.respond(mapOf("OK" to true))
        }
        get("/snippets") {
            call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start(true)
}