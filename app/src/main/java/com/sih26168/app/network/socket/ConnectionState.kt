package com.sih26168.app.network.socket

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Reconnecting : ConnectionState()
    data class Failed(val error: String? = null) : ConnectionState()
}
