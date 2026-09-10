package com.sih26168.app.network.socket

import android.util.Log
import com.sih26168.app.network.api.ApiClient
import com.sih26168.app.network.api.dto.BackendErrorDto
import com.sih26168.app.network.api.dto.FusionUpdateDto
import com.sih26168.app.network.api.dto.TripStatusDto
import com.sih26168.app.network.config.BackendConfig
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

private const val TAG = "SocketManager"

class SocketManager private constructor() {

    private var socket: Socket? = null
    private var currentTripId: String? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // fusion_update events — real-time AI/INS fusion output
    private val _fusionUpdates = MutableSharedFlow<FusionUpdateDto>(replay = 0)
    val fusionUpdates: SharedFlow<FusionUpdateDto> = _fusionUpdates.asSharedFlow()

    // trip_status events — server-side trip lifecycle notifications
    private val _tripStatusUpdates = MutableSharedFlow<TripStatusDto>(replay = 0)
    val tripStatusUpdates: SharedFlow<TripStatusDto> = _tripStatusUpdates.asSharedFlow()

    // error events — application-level errors from the backend
    private val _errors = MutableSharedFlow<BackendErrorDto>(replay = 0)
    val errors: SharedFlow<BackendErrorDto> = _errors.asSharedFlow()

    fun connect(tripId: String, authToken: String?) {
        // Clean up any existing connection first
        disconnect()

        currentTripId = tripId
        _connectionState.value = ConnectionState.Connecting

        val url = BackendConfig.socketUrl()
        val opts = IO.Options().apply {
            // Socket.IO natively implements exponential backoff via the Backoff class.
            // We configure the base delay and ceiling; the client handles the rest.
            reconnection = true
            reconnectionDelay = 1_000L       // initial backoff (ms)
            reconnectionDelayMax = 5_000L     // ceiling for backoff (ms)
            reconnectionAttempts = Int.MAX_VALUE
            timeout = 10_000L               // connection timeout (ms)
            forceNew = true

            // TODO: confirm with backend team — exact auth field name/structure
            if (authToken != null) {
                auth = mapOf("token" to authToken)
            }
        }

        socket = IO.socket(url, opts)
        setupListeners()
        socket?.connect()
    }

    fun disconnect() {
        val s = socket
        if (s != null) {
            s.close()
        }
        socket = null
        currentTripId = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun setupListeners() {
        val socket = this.socket ?: return

        socket.on(Socket.EVENT_CONNECT) {
            _connectionState.value = ConnectionState.Connected
            sendHandshake()
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            _connectionState.value = ConnectionState.Disconnected
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val errMsg = args.firstOrNull()?.toString()
            // Only mark as Failed if we never reached CONNECTED (initial connection failure).
            // Reconnection errors are surfaced via reconnect_attempt / reconnect_failed.
            if (_connectionState.value is ConnectionState.Connecting) {
                _connectionState.value = ConnectionState.Failed(errMsg)
            }
        }

        // Manager-level reconnection events (exposed via socket.io()).
        val manager = socket.io()
        manager?.on(Manager.EVENT_RECONNECT_ATTEMPT) {
            _connectionState.value = ConnectionState.Reconnecting
        }

        manager?.on(Manager.EVENT_RECONNECT_FAILED) {
            _connectionState.value = ConnectionState.Failed(null)
        }

        manager?.on(Manager.EVENT_RECONNECT) {
            _connectionState.value = ConnectionState.Connected
        }

        // Backend custom events
        socket.on("fusion_update") { args ->
            (args.firstOrNull() as? JSONObject)?.let { obj ->
                parseDto<FusionUpdateDto>(obj)?.let { _fusionUpdates.tryEmit(it) }
            }
        }

        socket.on("trip_status") { args ->
            (args.firstOrNull() as? JSONObject)?.let { obj ->
                parseDto<TripStatusDto>(obj)?.let { _tripStatusUpdates.tryEmit(it) }
            }
        }

        socket.on("error") { args ->
            (args.firstOrNull() as? JSONObject)?.let { obj ->
                parseDto<BackendErrorDto>(obj)?.let { _errors.tryEmit(it) }
            }
        }
    }

    private fun sendHandshake() {
        val tripId = currentTripId ?: return
        val socket = socket ?: return

        // TODO: confirm with backend team — event name "handshake" and payload fields
        val payload = JSONObject().apply {
            put("tripId", tripId)
            put("deviceId", buildDeviceId())
            put("deviceModel", android.os.Build.MODEL)
            put("manufacturer", android.os.Build.MANUFACTURER)
            put("androidVersion", android.os.Build.VERSION.RELEASE)
            put("appVersion", "1.0")
        }
        socket.emit("handshake", payload)
    }

    private fun buildDeviceId(): String {
        return android.os.Build.ID + "_" + android.os.Build.DEVICE
    }

    private inline fun <reified T> parseDto(obj: JSONObject): T? {
        return try {
            ApiClient.json.decodeFromString<T>(obj.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse ${T::class.simpleName}: ${e.message}")
            null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SocketManager? = null

        fun getInstance(): SocketManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: SocketManager().also { INSTANCE = it }
        }
    }
}
