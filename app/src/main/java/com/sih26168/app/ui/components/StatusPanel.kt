package com.sih26168.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sih26168.app.gnss.GnssStatus
import com.sih26168.app.navigation.NavigationMode
import com.sih26168.app.ui.theme.SurfaceDark

@Composable
fun StatusPanel(
    gnssStatus: GnssStatus,
    mode: NavigationMode,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        GnssStatusBadge(status = gnssStatus)
    }
}
