package com.sih26168.app.search

import androidx.compose.runtime.snapshots.SnapshotStateList

class SearchResultsAdapter {
    val items: SnapshotStateList<NominatimResult> = SnapshotStateList()

    fun submitList(newItems: List<NominatimResult>) {
        items.clear()
        items.addAll(newItems)
    }

    fun clear() {
        items.clear()
    }

    val size: Int get() = items.size

    operator fun get(index: Int): NominatimResult = items[index]
}
