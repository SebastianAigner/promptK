import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        IpUtils_iosKt.ip = getWiFiAddress()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

// Return IP address of WiFi interface (en0) as a String, or `nil`
func getWiFiAddress() -> String? {
    var address: String?

    // Get list of all interfaces on the local machine:
    var ifaddr: UnsafeMutablePointer<ifaddrs>?
    guard getifaddrs(&ifaddr) == 0 else {
        return nil
    }
    guard let firstAddr = ifaddr else {
        return nil
    }

    // For each interface ...
    for ifptr in sequence(first: firstAddr, next: { $0.pointee.ifa_next }) {
        let interface = ifptr.pointee

        // Check for IPv4 or IPv6 interface:
        let addrFamily = interface.ifa_addr.pointee.sa_family
        if addrFamily == UInt8(AF_INET) /*|| addrFamily == UInt8(AF_INET6)*/ {
            // TODO: We commented out the IPv6 support because right now, we just need IPv4.
            // TODO: That's not very future-proof of us, so it might be worth returning to.

            // Check interface name:
            let name = String(cString: interface.ifa_name)
            if name == "en0" {

                // Convert interface address to a human readable string:
                var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                getnameinfo(interface.ifa_addr, socklen_t(interface.ifa_addr.pointee.sa_len),
                    &hostname, socklen_t(hostname.count),
                    nil, socklen_t(0), NI_NUMERICHOST)
                address = String(cString: hostname)
            }
        }
    }
    freeifaddrs(ifaddr)

    return address
}