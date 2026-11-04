package com.kampplus.hava.ui.forecast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.WeatherAssistantButton
import com.kampplus.hava.ui.theme.HavaTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Tahmin ekranının bağlayıcı katmanı; ViewModel'i edinir ve state'i aktarır.
 * (Bkz. [com.kampplus.hava.ui.city.CityRoute] — aynı desen.)
 */
@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

/**
 * Bir şehrin hava tahmini.
 *
 * Üç bölüm: anlık durum başlığı, saatlik yatay liste, günlük dikey liste.
 * Dört durumun tamamı burada da karşılanır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val condition = (uiState as? LoadState.Success)?.data?.current?.weatherCode?.let(WeatherCondition::fromWeatherCode)
        ?: WeatherCondition.PARTLY_CLOUDY
    Box(
        modifier = modifier.fillMaxSize().background(Brush.verticalGradient(skyColors(condition)))
    ) {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            WeatherAssistantButton(
                forecast = (uiState as? LoadState.Success)?.data,
                cityName = cityName
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(cityName.ifEmpty { stringResource(R.string.detail_title) }, color = Color.White)
                        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                // Tek bir kart beklendiği için burada Shimmer yerine basit gösterge yeterli.
                LoadState.Loading -> CircularProgressIndicator()
                LoadState.Empty -> ErrorView(message = "Bu şehir için tahmin bulunamadı.")
                is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                is LoadState.Success -> ForecastContent(subtitle = subtitle, forecast = uiState.data)
            }
        }
      }
    }
}

@Composable
private fun ForecastContent(subtitle: String, forecast: Forecast, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // İçerik ekrana sığmayabilir; dikey kaydırma ekliyoruz.
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CurrentWeatherHeader(
            cityName = subtitle,
            current = forecast.current,
            today = forecast.daily.firstOrNull(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        WeatherInsights(forecast = forecast)
        SectionTitle(stringResource(R.string.detail_hourly), "Önümüzdeki saatler")
        HourlyRow(hours = forecast.hourly)
        SectionTitle(stringResource(R.string.detail_daily), "Haftalık görünüm")
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.12f),
            contentColor = Color.White,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
        ) {
          Column {
            forecast.daily.forEachIndexed { index, day ->
                // İlk satır hariç her satırın üstüne ayırıcı çizgi.
                if (index > 0) HorizontalDivider()
                DailyRow(day = day, isToday = index == 0)
            }
          }
        }
    }
}

@Composable
private fun WeatherInsights(forecast: Forecast) {
    val nextRain = forecast.hourly.firstOrNull { (it.precipitationProbability ?: 0) >= 60 }
    val strongWind = (forecast.current.windSpeedKmh ?: 0.0) >= 40.0
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (nextRain != null || strongWind) {
            InsightCard(
                icon = "⚠️",
                title = "Hava uyarısı",
                message = when {
                    strongWind -> "Rüzgâr ${forecast.current.windSpeedKmh?.roundToInt()} km/sa hızında. Dışarıda dikkatli ol."
                    else -> "Saat ${nextRain!!.time.format(HOUR_FORMAT)} civarında yağış bekleniyor."
                },
                highlight = true
            )
        }
    }
}

@Composable
private fun InsightCard(icon: String, title: String, message: String, highlight: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = if (highlight) 0.2f else 0.12f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (highlight) 0.25f else 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(message, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.84f))
            }
        }
    }
}

/** Büyük sıcaklık + durum etiketi + hissedilen/nem/rüzgâr üçlüsü. */
@Composable
private fun CurrentWeatherHeader(cityName: String, current: CurrentWeather, today: DailyForecast?, modifier: Modifier = Modifier) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val dateText = current.observedAt.format(DateTimeFormatter.ofPattern("EEEE, d MMMM · HH:mm", Locale.forLanguageTag("tr")))
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White.copy(alpha = 0.13f),
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
    ) {
      Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(cityName, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.84f))
        Text(dateText.replaceFirstChar(Char::titlecase), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.74f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(condition.emoji, style = MaterialTheme.typography.displayLarge)
            Column {
                AnimatedContent(
                    targetState = degrees(current.temperatureC),
                    transitionSpec = { (fadeIn() + slideInVertically { it / 3 }) togetherWith (fadeOut() + slideOutVertically { -it / 3 }) },
                    label = "temperature-change"
                ) { temperature -> Text(temperature, style = MaterialTheme.typography.displayLarge, color = Color.White) }
                Text(condition.label, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Hissedilen ${degrees(current.apparentTemperatureC ?: current.temperatureC)}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
            today?.let { Text("${degrees(it.minTemperatureC)} / ${degrees(it.maxTemperatureC)}", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f)) }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.22f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            current.humidityPercent?.let { Metric(stringResource(R.string.detail_humidity), "$it%") }
            current.windSpeedKmh?.let { Metric(stringResource(R.string.detail_wind), "${it.roundToInt()} km/sa") }
            today?.precipitationProbability?.takeIf { it > 0 }?.let { Metric("Yağış", "%$it") }
        }
      }
    }
}

/**
 * Önümüzdeki 24 saat, yatay kaydırılabilir kartlar hâlinde.
 *
 * Servis günün başından itibaren veri gönderir; geçmiş saatleri gizlemek için
 * şu anki saatten öncekiler elenir.
 */
@Composable
private fun HourlyRow(hours: List<HourlyForecast>, modifier: Modifier = Modifier) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT)
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = upcoming, key = { it.time.toString() }) { hour ->
            Surface(
                modifier = Modifier.width(78.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = hour.time.format(HOUR_FORMAT), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = WeatherCondition.fromWeatherCode(hour.weatherCode).emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(text = degrees(hour.temperatureC), style = MaterialTheme.typography.titleMedium)
                    Text(
                        // Boş string yerine boşluk: kartların yüksekliği eşit kalsın.
                        text = percent(hour.precipitationProbability) ?: " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }
        }
    }
}

/** Günlük tahmin satırı: gün adı · yağış · emoji · en düşük / en yüksek. */
@Composable
private fun DailyRow(day: DailyForecast, isToday: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) stringResource(R.string.today) else day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percent(day.precipitationProbability).orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = WeatherCondition.fromWeatherCode(day.weatherCode).emoji,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Text(
            text = degrees(day.minTemperatureC),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.66f),
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = degrees(day.maxTemperatureC),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.68f))
    }
}

private fun skyColors(condition: WeatherCondition): List<Color> = when (condition) {
    WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> listOf(Color(0xFF176B87), Color(0xFF174A70), Color(0xFF142C4E))
    WeatherCondition.PARTLY_CLOUDY, WeatherCondition.OVERCAST, WeatherCondition.FOG -> listOf(Color(0xFF526F82), Color(0xFF354F6B), Color(0xFF202F4B))
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> listOf(Color(0xFF425B70), Color(0xFF253E5C), Color(0xFF172A45))
    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> listOf(Color(0xFF64879A), Color(0xFF42627D), Color(0xFF263F5C))
    WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF453D66), Color(0xFF2E3558), Color(0xFF17233D))
    WeatherCondition.UNKNOWN -> listOf(Color(0xFF3F6075), Color(0xFF2C4564), Color(0xFF192B48))
}

/** 18.4 → "18°". Kullanıcıya ondalık göstermeye gerek yok. */
private fun degrees(celsius: Double) = "${celsius.roundToInt()}°"

/** Yağış olasılığı yalnızca sıfırdan büyükse anlamlıdır. */
private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private const val HOURLY_COUNT = 24
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("tr"))

@Preview(showBackground = true)
@Composable
private fun ForecastScreenPreview() {
    HavaTheme {
        ForecastScreen(
            cityName = "Ankara",
            subtitle = "Ankara, Türkiye",
            uiState = LoadState.Success(
                Forecast(
                    current = CurrentWeather(
                        temperatureC = 21.0,
                        weatherCode = 0,
                        observedAt = LocalDateTime.of(2026, 1, 15, 14, 0),
                        apparentTemperatureC = 20.0,
                        humidityPercent = 45,
                        windSpeedKmh = 12.0
                    ),
                    hourly = List(8) {
                        HourlyForecast(
                            time = LocalDateTime.now().plusHours(it.toLong()),
                            temperatureC = 18.0 + it,
                            weatherCode = 2,
                            precipitationProbability = it * 5
                        )
                    },
                    daily = List(7) {
                        DailyForecast(
                            date = LocalDate.of(2026, 1, 15).plusDays(it.toLong()),
                            minTemperatureC = 9.0 + it,
                            maxTemperatureC = 19.0 + it,
                            weatherCode = 61,
                            precipitationProbability = 40
                        )
                    }
                )
            ),
            onBack = {},
            onRetry = {}
        )
    }
}
