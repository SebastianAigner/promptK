import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.ExperimentalForeignApi
import platform.darwin.getifaddrs
import platform.darwin.ifaddrs
import kotlin.native.internal.NativePtr

@OptIn(ExperimentalForeignApi::class)
actual fun getIp(): String {
    return ip ?: "Called it too early!!"
//    val foo = CPointerVarOf<ifaddrs>(NativePtr.NULL)
//    getifaddrs()
}

var ip: String? = null
    set(value) {
        println("New IP is $value")
        field = value
    }
