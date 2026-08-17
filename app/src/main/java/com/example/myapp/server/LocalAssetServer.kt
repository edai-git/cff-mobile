package com.example.myapp.server

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class LocalAssetServer(
    private val context: Context,
    private val port: Int
) {
    private val TAG = "LocalAssetServer"
    private var serverSocket: ServerSocket? = null
    private var executor: ExecutorService? = null
    private val isRunning = AtomicBoolean(false)

    companion object {
        private val MIME_TYPES = mapOf(
            "html" to "text/html; charset=utf-8",
            "htm" to "text/html; charset=utf-8",
            "js" to "application/javascript; charset=utf-8",
            "mjs" to "application/javascript; charset=utf-8",
            "css" to "text/css; charset=utf-8",
            "json" to "application/json; charset=utf-8",
            "bin" to "application/octet-stream",
            "manifest" to "text/cache-manifest; charset=utf-8",
            "appcache" to "text/cache-manifest; charset=utf-8",
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "png" to "image/png",
            "gif" to "image/gif",
            "svg" to "image/svg+xml",
            "ico" to "image/x-icon",
            "wasm" to "application/wasm",
            "woff" to "font/woff",
            "woff2" to "font/woff2",
            "ttf" to "font/ttf",
            "otf" to "font/otf",
            "txt" to "text/plain; charset=utf-8",
            "xml" to "application/xml; charset=utf-8"
        )
    }

    @Synchronized
    fun start() {
        if (isRunning.get()) return

        try {
            serverSocket = ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"))
            serverSocket?.reuseAddress = true
            executor = Executors.newCachedThreadPool()
            isRunning.set(true)

            executor?.execute {
                acceptConnections()
            }

            Log.i(TAG, "Local asset server started on port $port")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server: ${e.message}", e)
            stop()
            throw e
        }
    }

    @Synchronized
    fun stop() {
        if (!isRunning.getAndSet(false)) return

        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket: ${e.message}")
        }
        serverSocket = null

        try {
            executor?.shutdownNow()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down executor: ${e.message}")
        }
        executor = null

        Log.i(TAG, "Local asset server stopped")
    }

    private fun acceptConnections() {
        while (isRunning.get()) {
            try {
                val socket = serverSocket?.accept() ?: break
                executor?.execute {
                    handleClient(socket)
                }
            } catch (e: Exception) {
                if (isRunning.get()) {
                    Log.e(TAG, "Error accepting connection: ${e.message}")
                }
            }
        }
    }

    private fun handleClient(socket: Socket) {
        val startTime = System.currentTimeMillis()
        val clientIp = socket.inetAddress?.hostAddress ?: "unknown"
        var requestMethod = "GET"
        var requestPath = "/"
        var statusCode = 200
        var bytesSent = 0L

        try {
            socket.soTimeout = 10000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val out = BufferedOutputStream(socket.getOutputStream())

            val requestLine = reader.readLine()
            if (requestLine.isNullOrBlank()) {
                socket.close()
                return
            }

            val parts = requestLine.split(" ")
            if (parts.isNotEmpty()) requestMethod = parts[0].uppercase()
            if (parts.size > 1) requestPath = parts[1]

            // Read remaining headers until empty line
            var headerLine: String?
            while (reader.readLine().also { headerLine = it } != null) {
                if (headerLine.isNullOrEmpty()) break
            }

            if (requestMethod == "OPTIONS") {
                statusCode = 204
                sendResponseHeaders(out, 204, "No Content", null, 0)
                out.flush()
                ServerState.addLog(
                    method = requestMethod,
                    path = requestPath,
                    statusCode = statusCode,
                    sizeBytes = 0,
                    durationMs = System.currentTimeMillis() - startTime,
                    clientIp = clientIp
                )
                socket.close()
                return
            }

            if (requestMethod != "GET" && requestMethod != "HEAD") {
                statusCode = 405
                val errorMsg = "Method Not Allowed".toByteArray(Charsets.UTF_8)
                bytesSent = errorMsg.size.toLong()
                sendResponseHeaders(out, 405, "Method Not Allowed", "text/plain; charset=utf-8", bytesSent)
                out.write(errorMsg)
                out.flush()
                ServerState.addLog(
                    method = requestMethod,
                    path = requestPath,
                    statusCode = statusCode,
                    sizeBytes = bytesSent,
                    durationMs = System.currentTimeMillis() - startTime,
                    clientIp = clientIp
                )
                socket.close()
                return
            }

            val resolvedAsset = resolveAssetPath(requestPath)
            if (resolvedAsset == null) {
                statusCode = 404
                val notFoundBody = "404 Not Found".toByteArray(Charsets.UTF_8)
                bytesSent = notFoundBody.size.toLong()
                sendResponseHeaders(out, 404, "Not Found", "text/plain; charset=utf-8", bytesSent)
                if (requestMethod == "GET") {
                    out.write(notFoundBody)
                }
                out.flush()
            } else {
                val data = loadAssetBytes(resolvedAsset.path)
                if (data == null) {
                    statusCode = 500
                    val errBody = "500 Internal Server Error".toByteArray(Charsets.UTF_8)
                    bytesSent = errBody.size.toLong()
                    sendResponseHeaders(out, 500, "Internal Server Error", "text/plain; charset=utf-8", bytesSent)
                    out.write(errBody)
                    out.flush()
                } else {
                    statusCode = 200
                    bytesSent = data.size.toLong()
                    val contentType = getContentType(resolvedAsset.path)
                    sendResponseHeaders(out, 200, "OK", contentType, bytesSent)
                    if (requestMethod == "GET") {
                        out.write(data)
                    }
                    out.flush()
                }
            }
        } catch (e: Exception) {
            statusCode = 500
            Log.e(TAG, "Error handling client request $requestPath: ${e.message}")
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}

            val duration = System.currentTimeMillis() - startTime
            ServerState.addLog(
                method = requestMethod,
                path = requestPath,
                statusCode = statusCode,
                sizeBytes = bytesSent,
                durationMs = duration,
                clientIp = clientIp
            )
        }
    }

    private data class ResolvedAsset(val path: String)

    private fun resolveAssetPath(rawUrl: String): ResolvedAsset? {
        val cleanUrl = try {
            val urlWithoutQuery = rawUrl.substringBefore('?').substringBefore('#')
            URLDecoder.decode(urlWithoutQuery, "UTF-8")
        } catch (_: Exception) {
            rawUrl.substringBefore('?').substringBefore('#')
        }

        // Prevent path traversal
        val safePath = cleanUrl
            .replace("\\", "/")
            .trimStart('/')
            .replace("../", "")
            .replace("./", "")

        val assetManager = context.assets

        val candidates = mutableListOf<String>()
        if (safePath.isEmpty()) {
            candidates.add("public/index.html")
            candidates.add("index.html")
        } else {
            candidates.add("public/$safePath")
            candidates.add("public/$safePath/index.html")
            candidates.add(safePath)
            candidates.add("$safePath/index.html")
        }

        for (candidate in candidates) {
            if (assetExists(assetManager, candidate)) {
                return ResolvedAsset(candidate)
            }
        }
        return null
    }

    private fun assetExists(assetManager: AssetManager, path: String): Boolean {
        return try {
            assetManager.open(path).use { true }
        } catch (_: Exception) {
            false
        }
    }

    private fun loadAssetBytes(path: String): ByteArray? {
        return try {
            context.assets.open(path).use { it.readBytes() }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading asset $path: ${e.message}")
            null
        }
    }

    private fun getContentType(path: String): String {
        val ext = path.substringAfterLast('.', "").lowercase()
        return MIME_TYPES[ext] ?: "application/octet-stream"
    }

    private fun sendResponseHeaders(
        out: BufferedOutputStream,
        statusCode: Int,
        statusText: String,
        contentType: String?,
        contentLength: Long
    ) {
        val sb = StringBuilder()
        sb.append("HTTP/1.1 $statusCode $statusText\r\n")
        sb.append("Access-Control-Allow-Origin: *\r\n")
        sb.append("Access-Control-Allow-Methods: GET, HEAD, OPTIONS\r\n")
        sb.append("Access-Control-Allow-Headers: *\r\n")
        sb.append("Cache-Control: no-cache\r\n")
        sb.append("Connection: close\r\n")
        if (contentType != null) {
            sb.append("Content-Type: $contentType\r\n")
        }
        sb.append("Content-Length: $contentLength\r\n")
        sb.append("\r\n")

        out.write(sb.toString().toByteArray(Charsets.UTF_8))
    }
}
