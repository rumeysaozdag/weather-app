package com.kampplus.hava.ui.route

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RouteAdviceViewModel(
    private val repository: WeatherRepository = OpenMeteoWeatherRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoadState<Forecast>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<Forecast>> = _uiState.asStateFlow()
    private var request: Job? = null

    fun load(city: City) {
        request?.cancel()
        request = viewModelScope.launch {
            _uiState.value = LoadState.Loading
            _uiState.value = try {
                LoadState.Success(repository.getForecast(city.latitude, city.longitude))
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Yol önerisi için tahmin alınamadı: bağlantı hatası", e)
                LoadState.Error("Tahmin alınamadı. İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "Yol önerisi için HTTP ${e.code()}", e)
                LoadState.Error("Hava servisi şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Yol önerisi için yanıt çözümlenemedi", e)
                LoadState.Error("Tahmin verisi okunamadı. Tekrar dene.")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Yol önerisi için eksik tahmin verisi", e)
                LoadState.Error("Bu şehir için yeterli tahmin verisi yok.")
            }
        }
    }

    fun retry(city: City) = load(city)

    private companion object {
        const val TAG = "RouteAdviceViewModel"
    }
}
