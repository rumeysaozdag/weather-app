package com.kampplus.hava.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RoutingApi {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path(value = "coordinates", encoded = true) coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("steps") steps: Boolean = false
    ): OsrmRouteResponse
}

data class OsrmRouteResponse(
    val code: String,
    val routes: List<OsrmRoute> = emptyList()
)

data class OsrmRoute(
    val distance: Double,
    val duration: Double,
    val geometry: OsrmGeometry
)

data class OsrmGeometry(val coordinates: List<List<Double>> = emptyList())
