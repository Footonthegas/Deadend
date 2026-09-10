package com.sih26168.app.search

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

data class RouteInfo(
    val distanceMeters: Double,
    val distanceKm: Double,
    val estimatedMinutes: Double,
    val etaMinutes: Double
)

class SearchMapController {
    private var destinationMarker: Marker? = null
    private var routePolyline: Polyline? = null
    private var currentRoute: RouteInfo? = null

    fun placeDestination(mapView: MapView?, point: GeoPoint, zoom: Double = 16.0) {
        val mv =mapView ?: return
        removeDestination(mv)

        val marker = Marker(mv).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
        destinationMarker = marker
        mv.overlays.add(marker)
        mv.controller.animateTo(point)
        mv.controller.setZoom(zoom)
    }

    fun drawRoute(mapView: MapView?, from: GeoPoint, to: GeoPoint, speedMps: Double? = null) {
        val mv =mapView ?: return
        removeRoute(mv)

        val line = Polyline().apply {
            color = 0xFF8AB4F8.toInt()
            width = 10f
            points.add(from)
            points.add(to)
        }
        routePolyline = line
        mv.overlays.add(line)
        mv.controller.animateTo(to)

        currentRoute = computeRouteInfo(from, to, speedMps)
    }

    fun getCurrentRoute(): RouteInfo? = currentRoute

    fun clear(mapView: MapView?) {
        val mv =mapView ?: return
        removeDestination(mv)
        removeRoute(mv)
        destinationMarker = null
        routePolyline = null
        currentRoute = null
    }

    private fun removeDestination(mapView: MapView) {
        destinationMarker?.let {mapView.overlays.remove(it) }
        destinationMarker = null
    }

    private fun removeRoute(mapView: MapView) {
        routePolyline?.let {mapView.overlays.remove(it) }
        routePolyline = null
    }

    private fun computeRouteInfo(from: GeoPoint, to: GeoPoint, speedMps: Double?): RouteInfo {
        val distM = haversineDistance(from.latitude, from.longitude, to.latitude, to.longitude)
        val distKm = distM / 1000.0
        val estMin = if (speedMps != null && speedMps > 0.5) {
            (distM / speedMps) / 60.0
        } else {
            (distM / (13.89 * 1000.0 / 60.0)) / 60.0
        }
        return RouteInfo(distM, distKm, estMin, estMin)
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}