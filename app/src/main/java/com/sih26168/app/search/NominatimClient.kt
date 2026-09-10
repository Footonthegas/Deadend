package com.sih26168.app.search

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.UnknownHostException
import java.net.ConnectException
import javax.net.ssl.SSLException

private const val TAG = "NominatimClient"

data class NominatimResult(
    val displayName: String,
    val lat: Double,
    val lon: Double
)

class NominatimClient {
    companion object {
        private const val USER_AGENT = "Deadend-NavApp/1.0 (+https://github.com/Footonthegas/Deadend)"
    }

    private val client = OkHttpClient()

    suspend fun search(query: String): List<NominatimResult> = withContext(Dispatchers.IO) {
        val url: HttpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("nominatim.openstreetmap.org")
            .addPathSegment("search")
            .addQueryParameter("q", query)
            .addQueryParameter("format", "json")
            .addQueryParameter("limit", "5")
            .build()

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept-Language", "en")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Nominatim request failed: ${response.code} ${response.message}")
                    return@withContext emptyList()
                }
                val body = response.body?.string()
                if (body.isNullOrEmpty()) {
                    Log.w(TAG, "Nominatim response body is empty")
                    return@withContext emptyList()
                }
                Log.d(TAG, "Nominatim response: ${body.take(200)}")
                parseResults(body)
            }
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Nominatim UnknownHost: ${e.message}")
            emptyList()
        } catch (e: ConnectException) {
            Log.e(TAG, "Nominatim ConnectException: ${e.message}")
            emptyList()
        } catch (e: SSLException) {
            Log.e(TAG, "Nominatim SSLException: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Nominatim search error: ${e.javaClass.simpleName}: ${e.message}")
            emptyList()
        }
    }

    private fun parseResults(json: String): List<NominatimResult> {
        val results = mutableListOf<NominatimResult>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val lat = obj.optString("lat")?.takeIf { it.isNotEmpty() } ?: continue
                val lon = obj.optString("lon")?.takeIf { it.isNotEmpty() } ?: continue
                val latD = lat.toDoubleOrNull() ?: continue
                val lonD = lon.toDoubleOrNull() ?: continue
                val name = obj.optString("display_name", "").ifEmpty {
                    obj.optString("display_name", "Unnamed place")
                }
                results.add(NominatimResult(name, latD, lonD))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse Nominatim results: ${e.message}")
        }
        return results
    }
}
