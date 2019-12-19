import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.lang.RuntimeException
import java.util.*

class InvalidCredentialsException(message: String): RuntimeException(message)

data class User(val name: String, val password: String)

data class LoginRegister(val user: String, val password: String)

data class Snippet(val user: String, val text: String)

data class PostSnippet(val snippet: PostSnippet.Text) {
    data class Text(val text: String)
}

val users = Collections.synchronizedMap(
    listOf(User("myname", "test"))
        .associateBy { it.name }
        .toMutableMap()
)

val snippets = Collections.synchronizedList(
    mutableListOf(
        Snippet("myname", "Hello"),
        Snippet("myname", "KTOR!")
    )
)

open class SimpleJWT(val secret: String) {

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT.require(algorithm).build()

    fun sign(name: String): String =
        JWT.create().withClaim("name", name).sign(algorithm)

}

fun Application.module() {

    install(StatusPages) {
        exception<InvalidCredentialsException> { e ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("Ok" to false, "error" to (e.message ?: "")))
        }
    }

    val simpleJwt = SimpleJWT("my-super-secret-for-jwt") // add secret somewhere else
    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    install(ContentNegotiation) {
        jackson { }
    }

    routing {
        post("/login-register") {
            val post = call.receive<LoginRegister>()
            val user = users.getOrPut(post.user) { User(post.user, post.password) }

            if (user.password != post.password) throw InvalidCredentialsException("Invalid credentials")

            call.respond(mapOf("token" to simpleJwt.sign(user.name)))
        }
        get("/") {
            call.respondText("My Example Blog", ContentType.Text.Html)
        }
        get("/status") {
            call.respond(mapOf("OK" to true))
        }
        route("/snippets") {
            get {
                call.respond(mapOf("snippets" to synchronized(snippets) { snippets.toList() }))
            }
            authenticate {
                post {
                    val post = call.receive<PostSnippet>()
                    val principal = call.principal<UserIdPrincipal>() ?: throw InvalidCredentialsException("No principal found")
                    snippets += Snippet(principal.name, post.snippet.text)
                    call.respond(mapOf("OK" to true))
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start(true)
}