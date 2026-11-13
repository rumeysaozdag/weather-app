package com.kampplus.hava.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TrafficStatus(
    val currentSpeedKmh: Int,
    val freeFlowSpeedKmh: Int,
    val currentTravelTimeSeconds: Int?,
    val freeFlowTravelTimeSeconds: Int?,
    val confidence: Double?,
    val roadClosed: Boolean
)

interface TrafficRepository {
    suspend fun getNearbyTraffic(latitude: Double, longitude: Double, apiKey: String): TrafficStatus
}

class TomTomTrafficRepository(
    private val api: TrafficApi = ApiClient.trafficApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TrafficRepository {
    override suspend fun getNearbyTraffic(latitude: Double, longitude: Double, apiKey: String): TrafficStatus =
        withContext(ioDispatcher) {
            val segment = api.getNearestRoadFlow(
                point = "$latitude,$longitude",
                apiKey = apiKey
            ).flowSegmentData ?: throw IllegalStateException("Yakın çevrede trafik verisi bulunamadı.")

            TrafficStatus(
                currentSpeedKmh = segment.currentSpeed ?: 0,
                freeFlowSpeedKmh = segment.freeFlowSpeed ?: 0,
                currentTravelTimeSeconds = segment.currentTravelTime,
                freeFlowTravelTimeSeconds = segment.freeFlowTravelTime,
                confidence = segment.confidence,
                roadClosed = segment.roadClosure == true
            )
        }
}
