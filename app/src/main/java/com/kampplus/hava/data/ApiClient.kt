package com.kampplus.hava.data

import com.kampplus.hava.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * İnternete giden isteklerin "garsonu".
 *
 * `object` = Kotlin'de tek örnekli (singleton) sınıf. Uygulama boyunca tek bir
 * Retrofit kurulumu yeterlidir; her istekte yeniden kurmak israf olurdu.
 *
 * `by lazy`: alan ilk kez okunduğunda kurulur. Uygulama açılırken değil, ilk
 * istek atıldığında maliyet ödenir.
 *
 * Not: Open-Meteo API anahtarı istemez. Gerçek projelerde anahtarlar koda değil,
 * `local.properties` veya `BuildConfig` üzerinden verilir.
 */
object ApiClient {

    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
    private const val AIR_QUALITY_BASE_URL = "https://air-quality-api.open-meteo.com/"
    private const val TRAFFIC_BASE_URL = "https://api.tomtom.com/"
    private const val ROUTING_BASE_URL = "https://router.project-osrm.org/"
    private const val RAINVIEWER_BASE_URL = "https://api.rainviewer.com/"

    private val httpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Traffic keys are passed as query parameters by TomTom. Keep this client free
    // of the DEBUG URL logger so the key is never printed to Logcat.
    private val trafficHttpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }

    /** Şehir arama istemcisi. Kullanımı: `ApiClient.cityApi.search("Ankara")` */
    val cityApi: CityApi by lazy { createApi(GEOCODING_BASE_URL) }

    /** Hava tahmini istemcisi. */
    val forecastApi: ForecastApi by lazy { createApi(FORECAST_BASE_URL) }

    /** Air quality and pollen data are served from Open-Meteo's separate endpoint. */
    val airQualityApi: AirQualityApi by lazy { createApi(AIR_QUALITY_BASE_URL) }

    /** TomTom live traffic flow for the road segment closest to a coordinate. */
    val trafficApi: TrafficApi by lazy {
        Retrofit.Builder()
            .baseUrl(TRAFFIC_BASE_URL)
            .client(trafficHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrafficApi::class.java)
    }

    /** Public OSRM demo routing endpoint; suitable for prototypes, not production traffic. */
    val routingApi: RoutingApi by lazy { createApi(ROUTING_BASE_URL) }

    /** Public RainViewer radar frame manifest; no API key is required. */
    val rainViewerApi: RainViewerApi by lazy { createApi(RAINVIEWER_BASE_URL) }

    /**
     * Verilen adres için bir Retrofit arayüzü üretir.
     *
     * `GsonConverterFactory`: gelen JSON'u data class'lara çeviren dönüştürücü.
     * `inline` + `reified T`: çağrı yerindeki tipi çalışma zamanında bilebilmek için;
     * böylece `CityApi::class.java` yazmaya gerek kalmaz.
     */
    private inline fun <reified T> createApi(baseUrl: String): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(T::class.java)
}
