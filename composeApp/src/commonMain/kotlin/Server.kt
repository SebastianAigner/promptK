import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.MutableSharedFlow

sealed interface TeleprompterEvent
data object Up : TeleprompterEvent
data object Down : TeleprompterEvent
data object ToggleMirroring : TeleprompterEvent

data class SetText(val text: String) : TeleprompterEvent

fun runServer(mutableSharedFlow: MutableSharedFlow<TeleprompterEvent>): ApplicationEngine {

    return embeddedServer(CIO, port = 8080) {

        routing {
            get("/") {
                call.respondText("Hello, banana!")
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
        }
    }.start()
}