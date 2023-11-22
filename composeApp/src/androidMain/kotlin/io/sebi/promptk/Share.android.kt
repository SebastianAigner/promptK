import android.content.Intent
import io.sebi.promptk.MainActivity

actual fun go(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share server address")
    MainActivity.context.startActivity(shareIntent)
}
