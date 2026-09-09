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
fun HeadingCard(headingDeg: Double?, modifier: Modifier = Modifier) {
    val headingStr = headingDeg?.let { String.format("%.0f°", it) } ?: "--°"
    
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = headingStr,
            color = PrimaryText,
            style = Typography.titleLarge
        )
        Text(
            text = "HEADING",
            color = SecondaryText,
            style = Typography.labelSmall
        )
    }
}
