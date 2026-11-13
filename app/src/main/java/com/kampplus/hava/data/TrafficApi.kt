package com.kampplus.hava.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** TomTom Traffic Flow Segment API: traffic on the road nearest the selected coordinates. */
interface TrafficApi {
    @GET("traffic/services/4/flowSegmentData/absolute/{zoom}/json")
    suspend fun getNearestRoadFlow(
        @Path("zoom") zoom: Int = 16,
        @Query("point") point: String,
        @Query("unit") unit: String = "kmph",
        @Query("key") apiKey: String
    ): TrafficFlowResponse
}

data class TrafficFlowResponse(
    val flowSegmentData: TrafficFlowSegment? = null
)

data class TrafficFlowSegment(
    val frc: String? = null,
    val currentSpeed: Int? = null,
    val freeFlowSpeed: Int? = null,
    val currentTravelTime: Int? = null,
    val freeFlowTravelTime: Int? = null,
    val confidence: Double? = null,
    val roadClosure: Boolean? = null
)
