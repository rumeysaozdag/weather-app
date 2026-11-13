package com.kampplus.hava.ui.forecast

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.data.TrafficStatus
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
 */
@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val trafficState by viewModel.trafficState.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        trafficState = trafficState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        latitude = viewModel.latitude,
        longitude = viewModel.longitude,
        modifier = modifier
    )
}

/**
 * Bir şehrin hava tahmini (Notion döküman ve özellik kartları görünümü).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    trafficState: LoadState<TrafficStatus> = LoadState.Empty,
    latitude: Double? = null,
    longitude: Double? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        floatingActionButton = {
            WeatherAssistantButton(
                forecast = (uiState as? LoadState.Success)?.data,
                cityName = cityName,
                latitude = latitude,
                longitude = longitude
            )
        },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = cityName.ifEmpty { stringResource(R.string.detail_title) },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (uiState is LoadState.Success) 0.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                LoadState.Loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                LoadState.Empty -> ErrorView(message = "Bu şehir için tahmin bulunamadı.")
                is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                is LoadState.Success -> ForecastContent(subtitle = subtitle, forecast = uiState.data, trafficState = trafficState)
            }
        }
    }
}

@Composable
private fun ForecastContent(
    subtitle: String,
    forecast: Forecast,
    trafficState: LoadState<TrafficStatus>,
    modifier: Modifier = Modifier
) {
    var selectedDateIso by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedDay = forecast.daily.firstOrNull { it.date.toString() == selectedDateIso }
    val selectedDayLabel = selectedDay?.let { day ->
        if (day.date == forecast.daily.firstOrNull()?.date) {
            stringResource(R.string.today)
        } else {
            day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 14.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CurrentWeatherHeader(
            cityName = subtitle,
            current = forecast.current,
            today = forecast.daily.firstOrNull(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        TrafficSummaryCard(state = trafficState)
        WeatherInsights(forecast = forecast)
        SectionTitle(
            title = stringResource(R.string.detail_hourly),
            subtitle = selectedDayLabel?.let { "$it · Saatlik görünüm" } ?: "Önümüzdeki 24 saat"
        )
        HourlyRow(
            hours = forecast.hourly,
            selectedDate = selectedDay?.date
        )
        SectionTitle(stringResource(R.string.detail_daily), "Saatlik tahmin için bir güne dokunun")
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                forecast.daily.forEachIndexed { index, day ->
                    val dateKey = day.date.toString()
                    val isSelected = selectedDateIso == dateKey
                    val dayHours = forecast.hourly
                        .filter { it.time.toLocalDate() == day.date }
                        .ifEmpty { forecast.hourly.take(HOURLY_COUNT) }
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    DailyRow(
                        day = day,
                        isToday = index == 0,
                        isSelected = isSelected,
                        onClick = {
                            selectedDateIso = if (isSelected) null else dateKey
                        }
                    )
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        DayHourlyPanel(
                            day = day,
                            isToday = index == 0,
                            hours = dayHours
                        )
                    }
                }
            }
        }
        SectionTitle("Atmosfer & Çevre", "Hava kalitesi, astronomi ve aktivite")
        AtmosphereDashboard(forecast = forecast)
    }
}

@Composable
private fun TrafficSummaryCard(state: LoadState<TrafficStatus>) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🚗", style = MaterialTheme.typography.titleLarge)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Yakın yol trafiği",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                when (state) {
                    LoadState.Loading -> Text(
                        text = "Trafik bilgisi yükleniyor…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LoadState.Empty -> Text(
                        text = "Canlı trafik bilgisi şu anda kullanılamıyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    is LoadState.Error -> Text(
                        text = "Canlı trafik bilgisi şu anda alınamıyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    is LoadState.Success -> {
                        val traffic = state.data
                        val ratio = if (traffic.freeFlowSpeedKmh > 0) {
                            traffic.currentSpeedKmh.toDouble() / traffic.freeFlowSpeedKmh
                        } else {
                            1.0
                        }
                        val status = when {
                            traffic.roadClosed -> "Yol kapalı"
                            ratio >= 0.8 -> "Akıcı"
                            ratio >= 0.6 -> "Orta yoğunlukta"
                            ratio >= 0.3 -> "Yoğun"
                            else -> "Çok yoğun"
                        }
                        Text(
                            text = "$status · ${traffic.currentSpeedKmh} km/sa (normalde ${traffic.freeFlowSpeedKmh} km/sa)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Text(
                    text = "Seçilen il merkezine en yakın yol kesimi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AtmosphereDashboard(forecast: Forecast) {
    val current = forecast.current
    val air = forecast.airQuality
    val today = forecast.daily.firstOrNull()
    val aqi = air?.europeanAqi
    val pollen = air?.pollenGrainsPerCubicMeter
    val runAdvice = when {
        aqi == null && pollen == null -> null
        (aqi ?: 0) >= 60 || (pollen ?: 0.0) >= 80.0 -> "Dışarıda koşu önerilmez"
        (aqi ?: 0) >= 40 || (pollen ?: 0.0) >= 30.0 -> "Hassas kişiler dikkatli olsun"
        else -> "Koşu için uygun"
    }
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricGridRow {
            MetricTile(
                icon = "◎",
                label = "AQI",
                value = aqi?.let { "$it · ${aqiLabel(it)}" } ?: "Veri yok",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                icon = "☀",
                label = "UV endeksi",
                value = air?.uvIndex?.let { "${it.oneDecimal()} · ${uvLabel(it)}" }
                    ?: current.uvIndex?.let { "${it.oneDecimal()} · ${uvLabel(it)}" }
                    ?: "Veri yok",
                modifier = Modifier.weight(1f)
            )
        }
        MetricGridRow {
            MetricTile(
                icon = "💧",
                label = "Nem",
                value = current.humidityPercent?.let { "%$it" } ?: "Veri yok",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                icon = "🌬",
                label = "Rüzgâr",
                value = current.windSpeedKmh?.let { "↘ ${it.roundToInt()} km/sa" } ?: "Veri yok",
                modifier = Modifier.weight(1f)
            )
        }
        MetricGridRow {
            MetricTile(
                icon = "❄",
                label = "Çiy noktası",
                value = current.dewPointC?.let { "${it.roundToInt()}°" } ?: "Veri yok",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                icon = "↕",
                label = "Basınç",
                value = current.pressureHpa?.let { "${it.roundToInt()} hPa" } ?: "Veri yok",
                modifier = Modifier.weight(1f)
            )
        }
        MetricGridRow {
            MetricTile(
                icon = "◉",
                label = "Görünürlük",
                value = current.visibilityMeters?.let { "${(it / 1000).oneDecimal()} km" } ?: "Veri yok",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                icon = "🌾",
                label = "Polen",
                value = pollen?.let { "${pollenLabel(it)} · ${it.roundToInt()}" } ?: "Mevsim dışı / veri yok",
                modifier = Modifier.weight(1f)
            )
        }
        if (today?.sunrise != null || today?.sunset != null) {
            SunArcCard(today = today, now = current.observedAt)
        }
        if (today?.moonrise != null || today?.moonset != null || today?.moonPhase != null) {
            MoonCard(today = today)
        }
        if (runAdvice != null) {
            MetricTile(
                icon = "🏃",
                label = "Koşu önerisi",
                value = runAdvice,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = "Hava: Open-Meteo · Hava kalitesi: CAMS / Open-Meteo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp, top = 4.dp)
        )
    }
}

@Composable
private fun MetricGridRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun MetricTile(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$icon  $label",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SunArcCard(today: DailyForecast, now: LocalDateTime) {
    val sunrise = today.sunrise
    val sunset = today.sunset
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val activeColor = MaterialTheme.colorScheme.tertiary

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "☀  Güneş Yörüngesi",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                val baseY = size.height - 4.dp.toPx()
                val radius = minOf(size.width / 2f, baseY)
                val arcTopLeft = Offset(0f, baseY - radius)
                val arcSize = Size(size.width, radius * 2f)
                drawArc(
                    color = trackColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(2.dp.toPx())
                )
                val progress = if (sunrise != null && sunset != null && sunset.isAfter(sunrise)) {
                    (ChronoUnit.SECONDS.between(sunrise, now).toFloat() / ChronoUnit.SECONDS.between(sunrise, sunset)).coerceIn(0f, 1f)
                } else {
                    0f
                }
                drawArc(
                    color = activeColor,
                    startAngle = 180f,
                    sweepAngle = 180f * progress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round)
                )
                val angle = Math.PI * (1f - progress)
                val sun = Offset(
                    x = size.width / 2f + radius * kotlin.math.cos(angle).toFloat(),
                    y = baseY - radius * kotlin.math.sin(angle).toFloat()
                )
                drawCircle(activeColor.copy(alpha = 0.22f), 11.dp.toPx(), sun)
                drawCircle(activeColor, 6.dp.toPx(), sun)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AstroTime("Gün doğumu", sunrise)
                AstroTime("Gün batımı", sunset, end = true)
            }
        }
    }
}

@Composable
private fun MoonCard(today: DailyForecast) {
    val phase = today.moonPhase
    val moon = phase?.let { moonEmoji(it) } ?: "🌙"
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AstroTime("Ay doğumu", today.moonrise)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(moon, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = phase?.let(::moonPhaseLabel) ?: "Ay evresi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AstroTime("Ay batımı", today.moonset, end = true)
        }
    }
}

@Composable
private fun AstroTime(label: String, time: LocalDateTime?, end: Boolean = false) {
    Column(horizontalAlignment = if (end) Alignment.End else Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = time?.format(HOUR_FORMAT) ?: "—",
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun aqiLabel(value: Int) = when {
    value <= 20 -> "İyi"
    value <= 40 -> "Makul"
    value <= 60 -> "Orta"
    value <= 80 -> "Kötü"
    value <= 100 -> "Çok kötü"
    else -> "Çok yüksek"
}

private fun uvLabel(value: Double) = when {
    value < 3 -> "Düşük"
    value < 6 -> "Orta"
    value < 8 -> "Yüksek"
    value < 11 -> "Çok yüksek"
    else -> "Aşırı"
}

private fun pollenLabel(value: Double) = when {
    value < 10 -> "Düşük"
    value < 50 -> "Orta"
    else -> "Yüksek"
}

private fun moonEmoji(phase: Double) = when {
    phase < 0.03 || phase >= 0.97 -> "🌑"
    phase < 0.22 -> "🌒"
    phase < 0.28 -> "🌓"
    phase < 0.47 -> "🌔"
    phase < 0.53 -> "🌕"
    phase < 0.72 -> "🌖"
    phase < 0.78 -> "🌗"
    else -> "🌘"
}

private fun moonPhaseLabel(phase: Double) = when {
    phase < 0.03 || phase >= 0.97 -> "Yeni ay"
    phase < 0.25 -> "Hilal"
    phase < 0.28 -> "İlk dördün"
    phase < 0.5 -> "Şişkin ay"
    phase < 0.53 -> "Dolunay"
    phase < 0.75 -> "Küçülen ay"
    phase < 0.78 -> "Son dördün"
    else -> "Hilal"
}

private fun Double.oneDecimal() = String.format(Locale.forLanguageTag("tr"), "%.1f", this)

@Composable
private fun WeatherInsights(forecast: Forecast) {
    val nextRain = forecast.hourly.firstOrNull { (it.precipitationProbability ?: 0) >= 60 }
    val strongWind = (forecast.current.windSpeedKmh ?: 0.0) >= 40.0
    if (nextRain != null || strongWind) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
        shape = RoundedCornerShape(8.dp),
        color = if (highlight) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (highlight) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Büyük sıcaklık + durum etiketi + hissedilen/nem/rüzgâr üçlüsü. */
@Composable
private fun CurrentWeatherHeader(
    cityName: String,
    current: CurrentWeather,
    today: DailyForecast?,
    modifier: Modifier = Modifier
) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val dateText = current.observedAt.format(
        DateTimeFormatter.ofPattern("EEEE, d MMMM · HH:mm", Locale.forLanguageTag("tr"))
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cityName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateText.replaceFirstChar(Char::titlecase),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = condition.emoji,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
                Column {
                    AnimatedContent(
                        targetState = degrees(current.temperatureC),
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it / 3 }) togetherWith
                                (fadeOut() + slideOutVertically { -it / 3 })
                        },
                        label = "temperature-change"
                    ) { temperature ->
                        Text(
                            text = temperature,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = condition.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hissedilen ${degrees(current.apparentTemperatureC ?: current.temperatureC)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                today?.let {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "${degrees(it.minTemperatureC)} / ${degrees(it.maxTemperatureC)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                current.humidityPercent?.let { Metric(stringResource(R.string.detail_humidity), "%$it") }
                current.windSpeedKmh?.let { Metric(stringResource(R.string.detail_wind), "${it.roundToInt()} km/sa") }
                today?.precipitationProbability?.takeIf { it > 0 }?.let { Metric("Yağış", "%$it") }
            }
        }
    }
}

@Composable
private fun HourlyRow(
    hours: List<HourlyForecast>,
    selectedDate: LocalDate? = null,
    modifier: Modifier = Modifier
) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = if (selectedDate != null) {
        hours.filter { it.time.toLocalDate() == selectedDate }
            .ifEmpty { hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT) }
    } else {
        hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT)
    }
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = upcoming, key = { it.time.toString() }) { hour ->
            HourlyCapsuleCard(hour = hour, highlighted = selectedDate != null)
        }
    }
}

@Composable
private fun HourlyCapsuleCard(
    hour: HourlyForecast,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(72.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = hour.time.format(HOUR_FORMAT),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = WeatherCondition.fromWeatherCode(hour.weatherCode).emoji,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = degrees(hour.temperatureC),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = percent(hour.precipitationProbability) ?: " ",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun DailyRow(
    day: DailyForecast,
    isToday: Boolean,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) {
                stringResource(R.string.today)
            } else {
                day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase)
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percent(day.precipitationProbability).orEmpty(),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = WeatherCondition.fromWeatherCode(day.weatherCode).emoji,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Text(
            text = degrees(day.minTemperatureC),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(38.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = degrees(day.maxTemperatureC),
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(38.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = if (isSelected) "▴" else "▾",
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun DayHourlyPanel(
    day: DailyForecast,
    isToday: Boolean,
    hours: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    val dayTitle = if (isToday) {
        stringResource(R.string.today)
    } else {
        day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase)
    }
    val dateShort = day.date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("tr")))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$dayTitle ($dateShort) · Saatlik Hava Durumu",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            day.uvIndexMax?.let { uv ->
                Text(
                    text = "UV Maks: ${uv.oneDecimal()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = hours, key = { "day-${day.date}-${it.time}" }) { hour ->
                HourlyCapsuleCard(hour = hour, highlighted = true)
            }
        }
        if (day.sunrise != null || day.sunset != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                day.sunrise?.let {
                    Text(
                        text = "☀ Doğuş: ${it.format(HOUR_FORMAT)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                day.sunset?.let {
                    Text(
                        text = "🌇 Batış: ${it.format(HOUR_FORMAT)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun degrees(celsius: Double) = "${celsius.roundToInt()}°"

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
