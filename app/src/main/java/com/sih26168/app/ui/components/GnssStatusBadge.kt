package com.sih26168.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sih26168.app.gnss.GnssStatus
import com.sih26168.app.ui.theme.*

data class GnssStatusDisplay(val text: String, val textColor: Color, val dotColor: Color)

@Composable
fun GnssStatusBadge(status: GnssStatus, modifier: Modifier = Modifier) {
    val display = when (status) {
        GnssStatus.AVAILABLE -> GnssStatusDisplay("GNSS: AVAILABLE", SuccessGreen, SuccessGreen)
        GnssStatus.DEGRADED -> GnssStatusDisplay("GNSS: DEGRADED", WarningYellow, WarningYellow)
        GnssStatus.OUTAGE -> GnssStatusDisplay("GNSS: OUTAGE", ErrorRed, ErrorRed)
        GnssStatus.WAITING_FOR_FIX -> GnssStatusDisplay("GNSS: WAITING", SecondaryText, WarningYellow)
        GnssStatus.PERMISSION_DENIED -> GnssStatusDisplay("GNSS: NO PERMISSION", ErrorRed, ErrorRed)
        else -> GnssStatusDisplay("GNSS: ${status.name}", SecondaryText, SecondaryText)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(display.dotColor)
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(
            text = display.text,
            color = display.textColor,
            style = Typography.labelSmall
        )
    }
}
