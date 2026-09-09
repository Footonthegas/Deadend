package com.sih26168.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sih26168.app.ui.theme.PrimaryText
import com.sih26168.app.ui.theme.SecondaryText
import com.sih26168.app.ui.theme.SurfaceDark
import com.sih26168.app.ui.theme.Typography

@Composable
fun ConfidenceCard(confidence: Double?, modifier: Modifier = Modifier) {
    val confidenceStr = confidence?.let { String.format("%.0f%%", it * 100) } ?: "0%"

    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = confidenceStr,
            color = PrimaryText,
            style = Typography.titleLarge
        )
        Text(
            text = "CONFIDENCE",
            color = SecondaryText,
            style = Typography.labelSmall
        )
    }
}
