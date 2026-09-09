package com.sih26168.app.maps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay

class VehiclePositionOverlay(
    context: Context,
    private var latitude: Double? = null,
    private var longitude: Double? = null,
    private var headingDeg: Double? = null
) : Overlay() {

    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8AB4F8.toInt()
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val markerSizePx = dpToPx(48f, context)

    private fun dpToPx(dp: Float, context: Context): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun updatePosition(lat: Double?, lon: Double?, heading: Double?) {
        latitude = lat
        longitude = lon
        headingDeg = heading
    }

    override fun draw(
        canvas: Canvas,
        projection: Projection
    ) {
        if (latitude == null || longitude == null || headingDeg == null) return

        val geoPoint = GeoPoint(latitude!!, longitude!!)
        val point = projection.toPixels(geoPoint, null)

        val px = point.x.toFloat()
        val py = point.y.toFloat()

        val halfW = markerSizePx * 0.25f
        val h = markerSizePx

        canvas.save()
        canvas.rotate(headingDeg!!.toFloat(), px, py - h * 0.3f)

        path.reset()
        path.moveTo(px, py - h * 0.3f)
        path.lineTo(px + halfW, py + h * 0.25f)
        path.lineTo(px, py + h * 0.1f)
        path.lineTo(px - halfW, py + h * 0.25f)
        path.close()

        canvas.drawPath(path, markerPaint)

        canvas.restore()
    }
}
