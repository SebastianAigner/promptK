import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

sealed interface TeleprompterEvent
data object Up : TeleprompterEvent
data object Down : TeleprompterEvent
data object ToggleMirroring : TeleprompterEvent
data object ToggleScroll : TeleprompterEvent

data class SetText(val text: String) : TeleprompterEvent

@OptIn(ExperimentalResourceApi::class)
fun runServer(mutableSharedFlow: MutableSharedFlow<TeleprompterEvent>): ApplicationEngine {
    return embeddedServer(CIO, port = 8080) {
        routing {
            get("/") {
                val bytes = resource("index.html").readBytes()
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
    }.start()
}
