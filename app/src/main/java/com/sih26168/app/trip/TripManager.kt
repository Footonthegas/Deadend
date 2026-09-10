package com.sih26168.app.trip

import android.util.Log
import com.sih26168.app.network.api.ApiClient
import com.sih26168.app.network.api.BackendApiService
import com.sih26168.app.network.api.dto.BackendErrorDto
import com.sih26168.app.network.api.dto.BatchUploadRequest
import com.sih26168.app.network.api.dto.CreateTripRequest
import com.sih26168.app.network.api.dto.FusionUpdateDto
import com.sih26168.app.network.api.dto.SensorEventDto
import com.sih26168.app.network.api.dto.TripStatusDto
import com.sih26168.app.network.socket.ConnectionState
import com.sih26168.app.network.socket.SocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "TripManager"
private const val FLUSH_INTERVAL_MS = 5_000L
private const val FLUSH_THRESHOLD = 50

class TripManager(
    private val deviceId: String,
    private val apiService: BackendApiService = ApiClient.apiService,
    private val socketManager: SocketManager = SocketManager.getInstance(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    private val bufferLock = Any()
    private val eventBuffer = mutableListOf<SensorEventDto>()
    private val isFlushing = AtomicBoolean(false)
    private var flushJob: Job? = null

    @Volatile
    private var currentTripId: String? = null

    val connectionState: StateFlow<ConnectionState> = socketManager.connectionState
    val fusionUpdates: SharedFlow<FusionUpdateDto> = socketManager.fusionUpdates
    val tripStatusUpdates: SharedFlow<TripStatusDto> = socketManager.tripStatusUpdates
    val errors: SharedFlow<BackendErrorDto> = socketManager.errors

    suspend fun startTrip(authToken: String? = null): String = withContext(Dispatchers.IO) {
        val response = apiService.createTrip(
            CreateTripRequest(deviceId = deviceId, authToken = authToken)
        )
        val tripId = response.tripId
        currentTripId = tripId
        socketManager.connect(tripId, authToken)
        startPeriodicFlush()
        tripId
    }

    fun recordEvent(event: SensorEventDto) {
        val tripId = currentTripId ?: return

        val shouldFlush = synchronized(bufferLock) {
            eventBuffer.add(event)
            eventBuffer.size >= FLUSH_THRESHOLD
        }

        if (shouldFlush) {
            coroutineScope.launch { flushEventsInternal(tripId) }
        }
    }

    suspend fun endTrip() = withContext(Dispatchers.IO) {
        val tripId = currentTripId ?: return@withContext

        flushJob?.cancel()
        flushJob?.join()
        flushJob = null

        flushEventsInternal(tripId)

        currentTripId = null

        try {
            apiService.endTrip(tripId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to end trip on backend: ${e.message}")
        }

        socketManager.disconnect()

        synchronized(bufferLock) {
            eventBuffer.clear()
        }
    }

    suspend fun loadReplay(tripId: String): List<SensorEventDto> = withContext(Dispatchers.IO) {
        apiService.loadReplay(tripId).events
    }

    private fun startPeriodicFlush() {
        flushJob = coroutineScope.launch {
            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                val tripId = currentTripId ?: continue
                flushEventsInternal(tripId)
            }
        }
    }

    private suspend fun flushEventsInternal(tripId: String) {
        if (!isFlushing.compareAndSet(false, true)) return

        var toUpload: List<SensorEventDto>? = null
        try {
            toUpload = synchronized(bufferLock) {
                if (eventBuffer.isEmpty()) return@synchronized null
                val snapshot = eventBuffer.toList()
                eventBuffer.clear()
                snapshot
            }

            if (toUpload == null || toUpload.isEmpty()) return

            apiService.uploadEvents(
                tripId = tripId,
                request = BatchUploadRequest(tripId = tripId, events = toUpload)
            )
        } catch (e: CancellationException) {
            toUpload?.let {
                synchronized(bufferLock) { eventBuffer.addAll(0, it) }
            }
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload ${toUpload?.size ?: 0} events: ${e.message}")
            toUpload?.let {
                synchronized(bufferLock) { eventBuffer.addAll(0, it) }
            }
        } finally {
            isFlushing.set(false)
        }
    }
}
