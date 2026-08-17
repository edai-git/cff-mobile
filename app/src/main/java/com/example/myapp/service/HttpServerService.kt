package com.example.myapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapp.MainActivity
import com.example.myapp.server.LocalAssetServer
import com.example.myapp.server.NetworkUtils
import com.example.myapp.server.ServerState

class HttpServerService : Service() {

    private val TAG = "HttpServerService"
    private var server: LocalAssetServer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    companion object {
        const val CHANNEL_ID = "cff_http_server_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.myapp.action.START_SERVER"
        const val ACTION_STOP = "com.example.myapp.action.STOP_SERVER"
        const val EXTRA_PORT = "com.example.myapp.extra.PORT"

        fun startService(context: Context, port: Int = 8080) {
            val intent = Intent(context, HttpServerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PORT, port)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, HttpServerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val port = intent.getIntExtra(EXTRA_PORT, 8080)
                startServer(port)
            }
            ACTION_STOP -> {
                stopServer()
            }
        }
        return START_NOT_STICKY
    }

    private fun startServer(port: Int) {
        try {
            server?.stop()
            server = LocalAssetServer(applicationContext, port)
            server?.start()

            acquireLocks()

            val addresses = NetworkUtils.getLocalIpAddresses(this)
            val urls = addresses.map { "http://${it.ipAddress}:$port/" }
            val primaryUrl = urls.firstOrNull() ?: "http://localhost:$port/"

            ServerState.setRunning(
                isRunning = true,
                port = port,
                urls = if (urls.isEmpty()) listOf("http://localhost:$port/") else urls
            )

            val notification = buildNotification("Host Server Running on $primaryUrl")
            startForeground(NOTIFICATION_ID, notification)

            Log.i(TAG, "Server started successfully on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server: ${e.message}", e)
            ServerState.setRunning(
                isRunning = false,
                port = port,
                error = e.message ?: "Failed to bind port"
            )
            stopServer()
        }
    }

    private fun stopServer() {
        try {
            server?.stop()
            server = null
            releaseLocks()
            ServerState.setRunning(false, ServerState.status.value.port)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
            Log.i(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server: ${e.message}")
        }
    }

    private fun acquireLocks() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "cff-mobile:HttpServerWakeLock"
            )?.apply {
                acquire(10 * 60 * 1000L /* 10 minutes or continuous */)
            }

            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                WifiManager.WIFI_MODE_FULL_LOW_LATENCY
            } else {
                @Suppress("DEPRECATION")
                WifiManager.WIFI_MODE_FULL_HIGH_PERF
            }
            wifiLock = wifiManager?.createWifiLock(
                wifiMode,
                "cff-mobile:HttpServerWifiLock"
            )?.apply {
                acquire()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire wake/wifi locks: ${e.message}")
        }
    }

    private fun releaseLocks() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            wakeLock = null

            wifiLock?.let {
                if (it.isHeld) it.release()
            }
            wifiLock = null
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing locks: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Local HTTP Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows status of embedded local HTTP asset server"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, HttpServerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CSSFontFace PS4 Host")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop Server", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
