package com.kampplus.hava.data

import retrofit2.http.GET

interface RainViewerApi {
    @GET("public/weather-maps.json")
    suspend fun getWeatherMaps(): RainViewerMapsResponse
}

data class RainViewerMapsResponse(
    val host: String,
    val radar: RainViewerRadar
)

data class RainViewerRadar(
    val past: List<RainViewerFrame> = emptyList(),
    val nowcast: List<RainViewerFrame> = emptyList()
)

data class RainViewerFrame(
    val time: Long,
    val path: String
)
