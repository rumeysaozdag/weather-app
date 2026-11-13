package com.kampplus.hava.data

import retrofit2.http.GET
import retrofit2.http.Query

/** Current European air quality, UV index and pollen values from Open-Meteo. */
interface AirQualityApi {
    @GET("v1/air-quality")
    suspend fun getCurrentAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponse

    companion object {
        const val CURRENT_FIELDS = "european_aqi,uv_index,grass_pollen,birch_pollen,ragweed_pollen,mugwort_pollen,olive_pollen,alder_pollen"
    }
}
