package com.sih26168.app.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sih26168.app.ui.theme.PrimaryAccent
import com.sih26168.app.ui.theme.SecondaryText
import com.sih26168.app.ui.theme.SurfaceDark
import com.sih26168.app.ui.theme.Typography
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

private const val DEBOUNCE_MS = 400L
private const val MIN_QUERY_LENGTH = 3

@Composable
fun LocationSearchBar(
    modifier: Modifier = Modifier,
    mapView: MapView?,
    currentLocation: GeoPoint?,
    speedMps: Double = 0.0,
    client: NominatimClient = remember { NominatimClient() },
    controller: SearchMapController = remember { SearchMapController() },
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by rememberSaveable { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var destinationPoint by remember { mutableStateOf<GeoPoint?>(null) }

    val adapter = remember { SearchResultsAdapter() }
    val searchJob: MutableState<Job?> = remember { mutableStateOf(null) }

    fun onSuggestionSelected(result: NominatimResult) {
        val point = GeoPoint(result.lat, result.lon)
        destinationPoint = point
        controller.placeDestination(mapView, point)
        adapter.clear()
        query = result.displayName
        isExpanded = false
        isSearching = false
        searchJob.value?.cancel()
        keyboardController?.hide()
    }

    fun onClearAll() {
        searchJob.value?.cancel()
        query = ""
        adapter.clear()
        isExpanded = false
        isSearching = false
        destinationPoint = null
        controller.clear(mapView)
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob.value?.cancel()

        if (newQuery.length < MIN_QUERY_LENGTH) {
            adapter.clear()
            isSearching = false
            if (newQuery.isEmpty()) {
                isExpanded = false
            }
            return
        }

        isSearching = true
        isExpanded = true
        searchJob.value = scope.launch {
            delay(DEBOUNCE_MS)
            if (!isActive) return@launch
            val fetched = client.search(newQuery)
            if (query == newQuery) {
                adapter.submitList(fetched)
                isSearching = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(SurfaceDark, RoundedCornerShape(50))
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            TextField(
                value = query,
                onValueChange = ::onQueryChange,
                placeholder = {
                    Text("Search location", style = Typography.bodyMedium, color = SecondaryText)
                },
                textStyle = TextStyle(
                    color = Color(0xFFE4E4E4),
                    fontSize = 15.sp
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFFE4E4E4),
                    unfocusedTextColor = Color(0xFFE4E4E4),
                    cursorColor = PrimaryAccent
                ),
                interactionSource = remember { MutableInteractionSource() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = PrimaryAccent,
                    strokeWidth = 2.dp
                )
            } else if (query.isNotEmpty()) {
                IconButton(onClick = { onClearAll() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = SecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
        }

        SearchResultsDropdown(
            adapter = adapter,
            isSearching = isSearching,
            isExpanded = isExpanded,
            onItemClick = { result -> onSuggestionSelected(result) }
        )

        if (destinationPoint != null) {
            Spacer(Modifier.height(8.dp))
            val route = controller.getCurrentRoute()
            val from = currentLocation
            if (from != null) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        controller.drawRoute(mapView, from, destinationPoint!!, speedMps)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
                ) {
                    Text(
                        text = if (route != null) "Route to here" else "Route to here",
                        color = Color(0xFF121212),
                        fontSize = 14.sp
                    )
                }
                route?.let {
                    RouteInfoCard(route = it)
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccent.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Waiting for location...",
                        color = Color(0xFF121212),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsDropdown(
    adapter: SearchResultsAdapter,
    isSearching: Boolean,
    isExpanded: Boolean,
    onItemClick: (NominatimResult) -> Unit
) {
    if (!isExpanded) return

    val hasItems = adapter.size > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
            .padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        if (isSearching && !hasItems) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Searching...", color = SecondaryText, style = Typography.bodySmall)
            }
        } else if (!isSearching && !hasItems) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No results found", color = SecondaryText, style = Typography.bodySmall)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(count = adapter.size) { index ->
                    val item = adapter[index]
                    SuggestionRow(item) { onItemClick(item) }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    item: NominatimResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = item.displayName,
            style = Typography.bodySmall,
            color = Color(0xFFE4E4E4),
            fontSize = 14.sp,
            maxLines = 2
        )
    }
}

@Composable
private fun RouteInfoCard(route: RouteInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Distance", style = Typography.bodySmall, color = SecondaryText, fontSize = 11.sp)
                Text(text = "%.1f km".format(route.distanceKm), style = Typography.bodyMedium, color = PrimaryAccent, fontSize = 15.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Travel", style = Typography.bodySmall, color = SecondaryText, fontSize = 11.sp)
                Text(text = "%.0f min".format(route.estimatedMinutes), style = Typography.bodyMedium, color = PrimaryAccent, fontSize = 15.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "ETA", style = Typography.bodySmall, color = SecondaryText, fontSize = 11.sp)
                Text(text = "%.0f min".format(route.etaMinutes), style = Typography.bodyMedium, color = PrimaryAccent, fontSize = 15.sp)
            }
        }
    }
}
