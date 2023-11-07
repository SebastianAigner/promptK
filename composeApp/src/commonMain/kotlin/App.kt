import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.server.engine.*
import kotlinx.coroutines.flow.MutableSharedFlow


inline fun Modifier.mirror(): Modifier {
    return this.graphicsLayer(rotationY = 180f)
}

val scrollAmount = 200

@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            Column(
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeContent)
            ) {
                var server by remember { mutableStateOf<ApplicationEngine?>(null) }
                val scrollState = rememberScrollState()
                var text by remember { mutableStateOf("") }
                var mirrored by remember { mutableStateOf(false) }

                val events = remember { MutableSharedFlow<TeleprompterEvent>() }

                LaunchedEffect(events) {
                    server = runServer(events)
                }

                Row(Modifier.padding(8.dp)) {
                    Button(onClick = {
                        if (server == null) {
                            server = runServer(events)
                        }
                    }) {
                        Text(text = "Start server")
                    }
                    Button(
                        onClick = {
                            server?.stop()
                            server = null
                        }
                    ) {
                        Text(text = "Stop server")
                    }
                    OutlinedButton(onClick = { mirrored = !mirrored }) {
                        Text(text = "Mirror")
                    }
                }

                PrompterText(
                    mirrored = mirrored,
                    text = text,
                    scrollState = scrollState,
                )

                LaunchedEffect(events) {
                    events.collect { event ->
                        when (event) {
                            Down -> scrollState.animateScrollTo(scrollState.value + scrollAmount)
                            Up -> scrollState.animateScrollTo(scrollState.value - scrollAmount)
                            ToggleMirroring -> mirrored = !mirrored
                            is SetText -> text = event.text
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrompterText(
    mirrored: Boolean,
    text: String,
    scrollState: ScrollState = rememberScrollState(),
) {
    Box(
        modifier = Modifier
            .run {
                if (mirrored) mirror() else this
            }
            .verticalScroll(scrollState),
    ) {
        Text(text, fontSize = 50.sp, lineHeight = 60.sp)
    }
}
