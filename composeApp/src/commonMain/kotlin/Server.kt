import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

sealed interface TeleprompterEvent
data object Up : TeleprompterEvent
data object Down : TeleprompterEvent
data object ToggleMirroring : TeleprompterEvent
data object ToggleScroll : TeleprompterEvent

data object PageLoaded : TeleprompterEvent

data class SetText(val text: String) : TeleprompterEvent

const val PORT = 8080

@OptIn(InternalResourceApi::class)
fun runServer(mutableSharedFlow: MutableSharedFlow<TeleprompterEvent>): ApplicationEngine {
    val server = embeddedServer(CIO, PORT) {
        routing {
            get("/") {
                mutableSharedFlow.emit(PageLoaded)
//                val bytes = resource("index.html").readBytes()
                val bytes = readResourceBytes("index.html")
                call.respondText(contentType = ContentType.Text.Html, text = bytes.decodeToString())
            }
            post("up") {
                mutableSharedFlow.emit(Up)
            }
            post("down") {
                mutableSharedFlow.emit(Down)
            }
            post("setText") {
                val text = call.receiveText()
                mutableSharedFlow.emit(SetText(text))
            }
            post("mirror") {
                mutableSharedFlow.emit(ToggleMirroring)
            }
            post("scroll") {
                mutableSharedFlow.emit(ToggleScroll)
            }
        }
    }
    println(getIp())
    return server.start()
}
