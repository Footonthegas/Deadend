package com.sih26168.app.gnss

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.location.*
import com.sih26168.app.common.Time
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

class GnssManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _status = MutableStateFlow(GnssStatus.WAITING_FOR_FIX)
    val status: StateFlow<GnssStatus> = _status

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): GnssSample? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                cont.resume(locationToSample(location)) {}
            } else {
                try {
                    val bestLocation = listOfNotNull(
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER),
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER),
                        locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    ).minByOrNull { it.accuracy }
                    cont.resume(if (bestLocation != null) locationToSample(bestLocation) else null) {}
                } catch (e: Exception) {
                    cont.resume(null) {}
                }
            }
        }.addOnFailureListener {
            cont.resume(null) {}
        }
    }

    @SuppressLint("MissingPermission")
    fun getFlow(): Flow<GnssSample> = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val sample = locationToSample(location)
                    _status.value = if (sample.valid) GnssStatus.AVAILABLE else GnssStatus.DEGRADED
                    trySend(sample)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    _status.value = GnssStatus.WAITING_FOR_FIX
                }
            }
        }

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMaxUpdateDelayMillis(2000L)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .build()
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            _status.value = GnssStatus.PERMISSION_DENIED
            close(e)
            return@callbackFlow
        } catch (e: Exception) {
            android.util.Log.e("SIH26168", "GnssManager: requestLocationUpdates failed", e)
            close(e)
            return@callbackFlow
        }

        awaitClose {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            } catch (e: Exception) {
                android.util.Log.e("SIH26168", "GnssManager: removeLocationUpdates failed", e)
            }
        }
    }

    private fun locationToSample(location: Location): GnssSample {
        return GnssSample(
            timestampNs = Time.monotonicTimestampNs(),
            valid = location.hasAccuracy() && location.accuracy < 100.0f,
            latitude = location.latitude,
            longitude = location.longitude,
            altitudeM = if (location.hasAltitude()) location.altitude else null,
            speedMps = if (location.hasSpeed()) location.speed.toDouble() else null,
            headingDeg = if (location.hasBearing()) location.bearing.toDouble() else null,
            horizontalAccuracyM = if (location.hasAccuracy()) location.accuracy else null
        )
    }
}
