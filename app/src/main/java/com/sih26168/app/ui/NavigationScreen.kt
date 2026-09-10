package com.sih26168.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sih26168.app.gnss.GnssStatus
import com.sih26168.app.gnss.CompassSpinStage
import com.sih26168.app.maps.MapRenderer
import com.sih26168.app.search.LocationSearchBar
import com.sih26168.app.ui.components.*
import com.sih26168.app.ui.theme.BackgroundDark
import com.sih26168.app.ui.theme.PrimaryAccent
import com.sih26168.app.ui.theme.SecondaryText
import com.sih26168.app.ui.theme.SurfaceDark
import com.sih26168.app.ui.theme.Typography
import com.sih26168.app.viewmodel.NavigationViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun NavigationScreen(viewModel: NavigationViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var mapView: MapView? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        MapRenderer(
            modifier = Modifier.fillMaxSize(),
            latitude = uiState.latitude,
            longitude = uiState.longitude,
            headingDeg = uiState.headingDeg,
            onMapViewReady = { mapView = it }
        )

        if (uiState.compassSpinStage != CompassSpinStage.DONE) {
            if (uiState.compassSpinStage == CompassSpinStage.COMPASS_SPIN) {
                CompassSpinOverlay(
                    roughHeadingDeg = uiState.roughHeadingDeg,
                    progress = uiState.compassSpinProgress,
                    onSkip = { viewModel.skipCompassSpin() }
                )
            } else {
                InitializationOverlay(
                    modifier = Modifier.align(Alignment.Center),
                    stage = uiState.compassSpinStage,
                    gnssStatus = uiState.gnssStatus,
                    sensorHealthy = uiState.sensorHealthy,
                    locationServiceEnabled = uiState.locationServiceEnabled
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(safeDrawingPadding())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            if (uiState.compassSpinStage == CompassSpinStage.DONE) {
                StatusPanel(
                    gnssStatus = uiState.gnssStatus,
                    mode = uiState.navigationMode
                )
                Spacer(Modifier.height(8.dp))
            }
LocationSearchBar(
                modifier = Modifier.fillMaxWidth(),
               mapView =mapView,
                currentLocation = uiState.latitude?.let { lat ->
                    uiState.longitude?.let { lon -> GeoPoint(lat, lon) }
                },
                speedMps = uiState.speedMps ?: 0.0
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(safeDrawingPadding())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeedCard(
                    speedMps = uiState.speedMps,
                    modifier = Modifier.weight(1f)
                )
                HeadingCard(
                    headingDeg = uiState.headingDeg,
                    modifier = Modifier.weight(1f)
                )
                ConfidenceCard(
                    confidence = uiState.confidence,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun InitializationOverlay(
    modifier: Modifier = Modifier,
    stage: CompassSpinStage,
    gnssStatus: GnssStatus,
    sensorHealthy: Boolean,
    locationServiceEnabled: Boolean
) {
    val titleText = when (stage) {
        CompassSpinStage.COMPASS_SPIN -> "SENSOR CALIBRATION"
        CompassSpinStage.WAITING_FOR_FIX -> "WAITING FOR FIX"
        CompassSpinStage.LEVELING -> "LEVELING"
        CompassSpinStage.YAW_ALIGNMENT -> "YAW ALIGNMENT"
        CompassSpinStage.DONE -> "READY"
    }

    val statusText = when (stage) {
        CompassSpinStage.WAITING_FOR_FIX -> when {
            !locationServiceEnabled -> "Location services disabled - enable GPS in device settings"
            gnssStatus == GnssStatus.PERMISSION_DENIED -> "Location permission denied"
            gnssStatus == GnssStatus.WAITING_FOR_FIX -> "Waiting for GPS fix..."
            gnssStatus == GnssStatus.DEGRADED -> "Weak GPS signal, waiting for better fix..."
            gnssStatus == GnssStatus.OUTAGE -> "GNSS outage - using dead reckoning"
            !sensorHealthy -> "Sensors unavailable"
            else -> "Waiting for GPS fix..."
        }
        CompassSpinStage.LEVELING -> "Leveling IMU sensors"
        CompassSpinStage.YAW_ALIGNMENT -> "Aligning yaw with GNSS course"
        else -> "Initializing..."
    }

    Column(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titleText,
            color = PrimaryAccent,
            style = Typography.titleLarge
        )
        Text(
            text = statusText,
            color = if (gnssStatus == GnssStatus.PERMISSION_DENIED || !locationServiceEnabled) Color(0xFFCF6679) else SecondaryText,
            style = Typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (stage == CompassSpinStage.WAITING_FOR_FIX && !locationServiceEnabled) {
            Text(
                text = "Go to Settings > Location and enable GPS",
                color = SecondaryText,
                style = Typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun safeDrawingPadding(): PaddingValues {
    return WindowInsets.safeDrawing.asPaddingValues()
}
