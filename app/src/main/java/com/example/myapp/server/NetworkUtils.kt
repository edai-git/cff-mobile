package com.example.myapp.server

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

data class NetworkAddress(
    val interfaceName: String,
    val ipAddress: String,
    val isWifi: Boolean,
    val isHotspot: Boolean
)

object NetworkUtils {

    fun getLocalIpAddresses(context: Context): List<NetworkAddress> {
        val addresses = mutableListOf<NetworkAddress>()
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue

                val isWifiName = intf.name.startsWith("wlan", ignoreCase = true)
                val isApName = intf.name.startsWith("ap", ignoreCase = true) || intf.name.startsWith("rndis", ignoreCase = true)

                val inetAddresses = Collections.list(intf.inetAddresses)
                for (inetAddr in inetAddresses) {
                    if (!inetAddr.isLoopbackAddress && inetAddr is Inet4Address) {
                        val hostAddress = inetAddr.hostAddress ?: continue
                        addresses.add(
                            NetworkAddress(
                                interfaceName = intf.displayName ?: intf.name,
                                ipAddress = hostAddress,
                                isWifi = isWifiName,
                                isHotspot = isApName
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return addresses
    }

    fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
