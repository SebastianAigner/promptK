import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.server.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


inline fun Modifier.mirror(): Modifier {
    return this.graphicsLayer(rotationY = 180f)
}

val scrollAmount = 200

enum class ServerState {
    Running, Stopped, Loading
}

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
                val events = remember { MutableSharedFlow<TeleprompterEvent>() }

                var serverState by remember { mutableStateOf(ServerState.Stopped) }

                var showConnectionPopup by remember { mutableStateOf(false) }

                suspend fun startServer() {
                    serverState = ServerState.Loading
                    server = withContext(Dispatchers.IO) { runServer(events) }
                    showConnectionPopup = true
                    serverState = ServerState.Running
                }

                suspend fun stopServer() {
                    serverState = ServerState.Loading
                    showConnectionPopup = false
                    withContext(Dispatchers.IO) { server?.stop() }
                    server = null
                    serverState = ServerState.Stopped
                }

                val scope = rememberCoroutineScope()
                val scrollState = rememberScrollState()

                var text by remember { mutableStateOf("") }
                var mirrored by remember { mutableStateOf(false) }
                var autoScroll by remember { mutableStateOf(false) }

                LaunchedEffect(events) {
                    startServer()
                }

                LaunchedEffect(autoScroll) {
                    if (autoScroll) {
                        while (true) {
                            scrollState.animateScrollTo(
                                value = scrollState.value + scrollAmount,
                                animationSpec = tween(1000, easing = LinearEasing),
                            )
                        }
                    }
                }

                LaunchedEffect(events) {
                    events.collect { event ->
                        when (event) {
                            Down -> scrollState.animateScrollTo(scrollState.value + scrollAmount)
                            Up -> scrollState.animateScrollTo(scrollState.value - scrollAmount)
                            ToggleMirroring -> mirrored = !mirrored
                            ToggleScroll -> autoScroll = !autoScroll
                            is SetText -> text = event.text
                            is PageLoaded -> showConnectionPopup = false
                        }
                    }
                }

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(showConnectionPopup) {
                    if (showConnectionPopup) {
                        val ip = withContext(Dispatchers.IO) { getIp() }
                        val address = "http://$ip:$PORT"
                        val result: SnackbarResult =
                            snackbarHostState.showSnackbar("Please use server at $address", actionLabel = "GO")
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                println("going")
                                go(address)
                            }

                            SnackbarResult.Dismissed -> {
                                println("dismissed")
                            }
                        }
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            snackbar = { snackbarData -> Snackbar(snackbarData) }
                        )
                    },
                ) {
                    Column {
                        Row(
                            Modifier.padding(8.dp).height(ButtonDefaults.MinHeight),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnimatedContent(
                                targetState = serverState,
                                modifier = Modifier.width(160.dp)//.fillMaxHeight()
                            ) { state ->
                                when (state) {
                                    ServerState.Running -> {
                                        Button(onClick = {
                                            scope.launch {
                                                stopServer()
                                            }
                                        }) {
                                            Icon(Icons.Default.Close, "Stop server")
                                            Text("Stop server")
                                        }
                                    }

                                    ServerState.Stopped -> {
                                        Button(onClick = {
                                            scope.launch {
                                                startServer()
                                            }
                                        }) {
                                            Icon(Icons.Default.PlayArrow, "Start server")
                                            Text("Start server")
                                        }
                                    }

                                    ServerState.Loading -> {
                                        Button(onClick = {}, enabled = false) {
                                            CircularProgressIndicator(Modifier.size(24.dp))
                                        }
                                    }
                                }
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
