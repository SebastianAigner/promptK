import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.server.engine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow


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
        val fadeAlpha = remember { Animatable(0f) }
        LaunchedEffect(fadeAlpha) {
            delay(600)
            fadeAlpha.animateTo(
                1f, animationSpec = tween(1500, easing = EaseInOutSine)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
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

            var text by remember { mutableStateOf(sampleText) }
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

            val blurAmount by animateDpAsState(
                if (showConnectionPopup) 16.dp else 0.dp,
                animationSpec = tween(1000)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.alpha(fadeAlpha.asState().value),
            ) {
                Column(
                    Modifier.fillMaxSize()
                        .blur(blurAmount)
//                        .then(if (showConnectionPopup) Modifier.blur(10.dp) else Modifier)
                ) {
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
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                AnimatedVisibility(showConnectionPopup, enter = fadeIn(), exit = fadeOut()) {
                    PopupDialog(Modifier)
                }
            }
        }
    }
}

@Composable
fun PopupDialog(modifier: Modifier = Modifier) {

    var address by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val ip = withContext(Dispatchers.IO) { getIp() }
        address = "http://$ip:$PORT"
    }

    Box(
        modifier
            .width(300.dp)
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Please use server at")
            Text(address,
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    go(address)
                })
        }
    }
}

@Composable
private fun PrompterText(
    mirrored: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    Box(
        modifier = modifier
            .then(if (mirrored) Modifier.mirror() else Modifier)
            .verticalScroll(scrollState),
    ) {
        Text(text, fontSize = 50.sp, lineHeight = 60.sp)
    }
}
