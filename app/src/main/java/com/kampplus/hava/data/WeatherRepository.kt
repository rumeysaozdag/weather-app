package com.kampplus.hava.data

import com.kampplus.hava.model.Forecast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.CancellationException

/**
 * Hava tahmini verisine erişimin tek kapısı.
 *
 * [CityRepository] ile aynı desen: arayüz + gerçek uygulama. Tahmin ekranı yalnızca
 * bu arayüzü tanır; Retrofit'i veya JSON'u bilmez.
 */
interface WeatherRepository {
    /** Verilen koordinat için anlık durum + saatlik + günlük tahmini getirir. */
    suspend fun getForecast(latitude: Double, longitude: Double): Forecast
}

/** Gerçek uygulama: tahmini Open-Meteo forecast servisinden alır. */
class OpenMeteoWeatherRepository(
    private val api: ForecastApi = ApiClient.forecastApi,
    private val airQualityApi: AirQualityApi = ApiClient.airQualityApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast = withContext(ioDispatcher) {
        coroutineScope {
            // Fetch both endpoints together. Air quality is optional so its outage
            // must not prevent the core weather forecast from appearing.
            val weather = async { api.getForecast(latitude = latitude, longitude = longitude).toForecast() }
            val airQuality = async {
                try {
                    airQualityApi.getCurrentAirQuality(latitude, longitude).current?.toAirQuality()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    null
                }
            }
            val weatherForecast = weather.await()
            weatherForecast.copy(airQuality = airQuality.await())
        }
    }
}
