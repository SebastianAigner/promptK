import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.view.WindowManager
import io.sebi.promptk.PromptApplication
import java.net.InetAddress

actual fun getIp(): String {
    // TODO do something nicer for API 31 and up
    // https://developer.android.com/reference/android/net/wifi/WifiManager#getConnectionInfo()

    val context = PromptApplication.context
    val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
}