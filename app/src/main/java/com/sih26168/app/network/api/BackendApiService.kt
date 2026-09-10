package com.sih26168.app.network.api

import com.sih26168.app.network.api.dto.BatchUploadRequest
import com.sih26168.app.network.api.dto.CreateTripRequest
import com.sih26168.app.network.api.dto.ReplayResponseDto
import com.sih26168.app.network.api.dto.TripDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApiService {
    @POST("trips")
    suspend fun createTrip(@Body request: CreateTripRequest): TripDto

    @POST("trips/{tripId}/end")
    suspend fun endTrip(@Path("tripId") tripId: String)

    @POST("trips/{tripId}/events")
    suspend fun uploadEvents(
        @Path("tripId") tripId: String,
        @Body request: BatchUploadRequest
    )

    @GET("trips/{tripId}/replay")
    suspend fun loadReplay(@Path("tripId") tripId: String): ReplayResponseDto
}
