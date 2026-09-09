package com.sih26168.app.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MapRenderer(
    modifier: Modifier = Modifier,
    latitude: Double?,
    longitude: Double?,
    headingDeg: Double?,
    initialZoom: Double = 16.0
) {
    val context = LocalContext.current
    var mapView: MapView? by remember { mutableStateOf(null) }
    var vehicleOverlay: VehiclePositionOverlay? by remember { mutableStateOf(null) }
    var hasAutoCentered: Boolean by remember { mutableStateOf(false) }

    val darkTileSource = remember {
        TileSourceFactory.MAPNIK
    }

    val mapViewRef = remember {
        MapView(context).apply {
            setTileSource(darkTileSource)
            setMultiTouchControls(true)
            controller.setZoom(initialZoom)
            controller.setCenter(GeoPoint(0.0, 0.0))
        }
    }

    LaunchedEffect(Unit) {
        vehicleOverlay = VehiclePositionOverlay(context)
        mapViewRef.overlays.add(vehicleOverlay)
    }

    DisposableEffect(Unit) {
        mapView = mapViewRef
        mapViewRef.onResume()
        onDispose {
            mapViewRef.onPause()
            mapViewRef.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapViewRef },
        update = { view ->
            vehicleOverlay?.updatePosition(latitude, longitude, headingDeg)

            if (latitude != null && longitude != null && !hasAutoCentered) {
                view.controller.setZoom(initialZoom)
                view.controller.setCenter(GeoPoint(latitude, longitude))
                hasAutoCentered = true
            }

            view.invalidate()
        }
    )
}
