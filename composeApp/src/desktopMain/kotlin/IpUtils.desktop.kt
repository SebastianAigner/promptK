actual fun getIp(): String {
    return try {
        val localhost = java.net.InetAddress.getLocalHost()
        localhost.hostAddress
    } catch (exception: Exception) {
        throw Exception("Couldn't fetch local IP: ${exception.message}")
    }
}