package com.kampplus.hava.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kampplus.hava.BuildConfig
import com.kampplus.hava.R
import com.kampplus.hava.data.ApiClient
import com.kampplus.hava.data.RainViewerMapsResponse
import com.kampplus.hava.data.TomTomTrafficRepository
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.WeatherCondition
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private data class ChatMessage(val text: String, val fromUser: Boolean)

private val TR_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
private val ASSISTANT_HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val ASSISTANT_DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", TR_LOCALE)

@Composable
private fun AssistantLogoBadge(
    badgeSize: Dp = 28.dp,
    iconSize: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(badgeSize),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.ic_weather_assistant),
                contentDescription = "Hava Asistanı Logosu",
                tint = Color.Unspecified,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/** Notion AI tarzı minimalist yüzen asistan butonu ve sohbet penceresi. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WeatherAssistantButton(
    forecast: Forecast?,
    cityName: String = "",
    latitude: Double? = null,
    longitude: Double? = null,
    modifier: Modifier = Modifier
) {
    var isChatOpen by rememberSaveable { mutableStateOf(false) }
    Surface(
        onClick = { isChatOpen = true },
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistantLogoBadge(badgeSize = 26.dp, iconSize = 18.dp)
            Text(
                text = "Asistan",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }

    if (isChatOpen) {
        ModalBottomSheet(
            onDismissRequest = { isChatOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            AssistantChat(forecast = forecast, cityName = cityName, latitude = latitude, longitude = longitude)
        }
    }
}

@Composable
private fun AssistantChat(forecast: Forecast?, cityName: String, latitude: Double?, longitude: Double?) {
    val scope = rememberCoroutineScope()
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                if (forecast == null) {
                    "Merhaba! Canlı yağış radarı durumunu hemen sorabilirsin. Bir şehrin tahminini açtığında ise saatlik/7 günlük hava durumunu, yol ve trafiği, hava kalitesini (AQI/polen), UV endeksini, spor uygunluğunu ve kıyafet önerilerini detaylıca yanıtlarım."
                } else {
                    "Merhaba! $cityName için canlı radar, saatlik/7 günlük tahmin, yol ve trafik, kıyafet/şemsiye, hava kalitesi (AQI & polen), UV, güneş/ay saatleri ve dış mekan spor uygunluğu hakkında her şeyi sorabilirsin."
                },
                fromUser = false
            )
        )
    }
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val send: (String) -> Unit = { question ->
        sendMessage(question, forecast, cityName, latitude, longitude, messages, scope)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp, max = 680.dp)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AssistantLogoBadge(badgeSize = 38.dp, iconSize = 24.dp)
            Column {
                Text(
                    text = "Hava Asistanı",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (forecast == null) "Canlı radar & genel hava asistanı" else "$cityName · Radar, Tahmin & Yaşam Rehberi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "📡 Radar durumu?" to "Canlı yağış radarı durumu nasıl?",
                "👕 Ne giysem?" to "Bugün ne giysem?",
                "☂️ Şemsiye?" to "Şemsiye almalı mıyım?",
                "🚗 Yol & trafik?" to "Yol ve trafik durumu nasıl?",
                "📅 Yarın nasıl?" to "Yarın hava nasıl olacak?",
                "🗓️ Hafta sonu?" to "Hafta sonu hava nasıl?",
                "🌿 Hava kalitesi?" to "Hava kalitesi ve polen durumu nasıl?",
                "🏃 Spor & koşu?" to "Bugün dışarıda koşu veya spor yapılır mı?",
                "☀️ Güneş & UV?" to "UV endeksi ve gün batımı kaçta?"
            ).forEach { (label, prompt) ->
                AssistChip(
                    onClick = { send(prompt) },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(messages) { _, message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    if (!message.fromUser) {
                        AssistantLogoBadge(
                            badgeSize = 26.dp,
                            iconSize = 16.dp,
                            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (message.fromUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (message.fromUser) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        border = if (message.fromUser) {
                            null
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        },
                        modifier = Modifier.fillMaxWidth(0.86f)
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Radar, yarın, UV, trafik, kombin sor…") },
                shape = RoundedCornerShape(8.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    send(input)
                    input = ""
                })
            )
            Button(
                onClick = {
                    send(input)
                    input = ""
                },
                enabled = input.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Gönder")
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

private fun sendMessage(
    question: String,
    forecast: Forecast?,
    cityName: String,
    latitude: Double?,
    longitude: Double?,
    messages: MutableList<ChatMessage>,
    scope: CoroutineScope
) {
    val cleanedQuestion = question.trim()
    if (cleanedQuestion.isEmpty()) return
    messages += ChatMessage(cleanedQuestion, fromUser = true)

    when {
        isRadarQuestion(cleanedQuestion) -> {
            scope.launch {
                val radarAnswer = try {
                    val radarResponse = ApiClient.rainViewerApi.getWeatherMaps()
                    buildRadarAnswer(radarResponse, forecast, cityName)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    buildFallbackRadarAnswer(forecast, cityName)
                }
                messages += ChatMessage(radarAnswer, fromUser = false)
            }
        }
        forecast != null && latitude != null && longitude != null && isTrafficQuestion(cleanedQuestion) -> {
            val apiKey = BuildConfig.TOMTOM_API_KEY
            if (apiKey.isBlank()) {
                messages += ChatMessage(
                    "Canlı trafik bilgisi şu anda kullanılamıyor. $cityName için hava koşullarına dayalı yol uyarısı: ${weatherRoadSummary(forecast)}",
                    fromUser = false
                )
            } else {
                scope.launch {
                    val answer = try {
                        val traffic = TomTomTrafficRepository().getNearbyTraffic(latitude, longitude, apiKey)
                        answerTraffic(cityName, traffic, forecast)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        "Canlı trafik servisine şu an ulaşılamadı. ${weatherRoadSummary(forecast)}"
                    }
                    messages += ChatMessage(answer, fromUser = false)
                }
            }
        }
        else -> {
            messages += ChatMessage(answerWeatherQuestion(cleanedQuestion, forecast, cityName), fromUser = false)
        }
    }
}

private fun String.toAsciiSearch(): String = lowercase(TR_LOCALE)
    .replace('ı', 'i')
    .replace('ğ', 'g')
    .replace('ü', 'u')
    .replace('ş', 's')
    .replace('ö', 'o')
    .replace('ç', 'c')
    .replace('â', 'a')
    .replace('î', 'i')
    .replace('û', 'u')

private fun isRadarQuestion(question: String): Boolean {
    val ascii = question.toAsciiSearch()
    return listOf(
        "radar",
        "rainviewer",
        "yagis harita",
        "kutle",
        "bulut",
        "uydu",
        "haritada yagis",
        "firtina yaklas"
    ).any { ascii.contains(it) }
}

private fun isTrafficQuestion(question: String): Boolean {
    val ascii = question.toAsciiSearch()
    return listOf("trafik", "yol durumu", "yolda", "surus", "araba kullan", "otoyol").any {
        ascii.contains(it)
    }
}

private fun buildRadarAnswer(
    radarResponse: RainViewerMapsResponse,
    forecast: Forecast?,
    cityName: String
): String {
    val pastFrames = radarResponse.radar.past
    val nowcastFrames = radarResponse.radar.nowcast
    val zoneId = ZoneId.systemDefault()

    val scanSummary = if (pastFrames.isNotEmpty()) {
        val firstTime = Instant.ofEpochSecond(pastFrames.first().time).atZone(zoneId).format(ASSISTANT_HOUR_FORMAT)
        val latestTime = Instant.ofEpochSecond(pastFrames.last().time).atZone(zoneId).format(ASSISTANT_HOUR_FORMAT)
        val nowcastNote = if (nowcastFrames.isNotEmpty()) {
            " Ayrıca ileriye dönük ${nowcastFrames.size} adet kısa vadeli radar tahmin karesi (nowcast) mevcut."
        } else {
            ""
        }
        "📡 Canlı RainViewer Yağış Radarı aktif: $firstTime – $latestTime aralığını kapsayan ${pastFrames.size} güncel radar tarama karesi alındı (son tarama: $latestTime).$nowcastNote"
    } else {
        "📡 RainViewer radar servisine bağlanıldı ancak şu an için geçmiş radar karesi dönmedi."
    }

    if (forecast == null) {
        return "$scanSummary\n\nBir şehir seçip detay ekranını açtığında o şehrin üzerindeki yağış kütlelerini ve saatlik radar beklentisini de analiz edebilirim. Alt menüdeki 'Harita' sekmesinden radar katmanını canlı oynatabilirsin."
    }

    return "$scanSummary\n\n${cityRadarEchoAnalysis(forecast, cityName)}"
}

private fun buildFallbackRadarAnswer(forecast: Forecast?, cityName: String): String {
    if (forecast == null) {
        return "📡 Canlı RainViewer radar sunucusuna şu anda ulaşılamadı. Alt menüdeki 'Harita' sekmesinden yağış radarını tekrar yenileyebilir veya bir şehir seçerek saatlik yağış kütle analizini sorabilirsin."
    }
    return "📡 Canlı radar görüntü servisine şu an erişilemedi; ancak $cityName için meteorolojik model verilerine göre radar/yağış kütle analizi şöyle:\n\n${cityRadarEchoAnalysis(forecast, cityName)}"
}

private fun cityRadarEchoAnalysis(forecast: Forecast, cityName: String): String {
    val current = forecast.current
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val next12Hours = forecast.hourly.filter { !it.time.isBefore(now) }.take(12).ifEmpty { forecast.hourly.take(12) }

    val activeEcho = when (condition) {
        WeatherCondition.THUNDERSTORM -> "Şu anda $cityName üzerinde kuvvetli fırtına/konvektif yağış kütlesi etkili (${condition.label})."
        WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> "Şu anda $cityName üzerinde aktif yağmur kütlesi geçişi görülüyor (${condition.label})."
        WeatherCondition.DRIZZLE -> "Şu anda bölgede hafif çisenti üreten alçak bulut kütlesi var."
        WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> "Şu anda $cityName üzerinde kar yağışı bırakan soğuk cephe kütlesi etkili."
        WeatherCondition.FOG -> "Şu anda radar ekolarında yağıştan ziyade yoğun sis/alçak stratus tabakası hakim."
        WeatherCondition.OVERCAST -> "Şu anda $cityName üzerinde kapalı bulut örtüsü var; radar ekolarında kuvvetli yağış görünmüyor."
        WeatherCondition.PARTLY_CLOUDY, WeatherCondition.MAINLY_CLEAR -> "Şu anda $cityName üzerinde parçalı/az bulut geçişleri var; anlık kuvvetli bir yağış ekosu görünmüyor."
        WeatherCondition.CLEAR -> "Şu anda $cityName üzerinde gökyüzü açık; radar taramasında yağış kütlesi bulunmuyor."
        else -> "Şu anki atmosfer durumu: ${condition.label}."
    }

    val rainyHours = next12Hours.filter {
        (it.precipitationProbability ?: 0) >= 40 ||
            WeatherCondition.fromWeatherCode(it.weatherCode) in setOf(
                WeatherCondition.DRIZZLE,
                WeatherCondition.RAIN,
                WeatherCondition.RAIN_SHOWERS,
                WeatherCondition.SNOW,
                WeatherCondition.SNOW_SHOWERS,
                WeatherCondition.THUNDERSTORM
            )
    }

    val peakHour = next12Hours.maxByOrNull { it.precipitationProbability ?: 0 }
    val echoWindow = if (rainyHours.isNotEmpty()) {
        val startHour = rainyHours.first().time.format(ASSISTANT_HOUR_FORMAT)
        val endHour = rainyHours.last().time.format(ASSISTANT_HOUR_FORMAT)
        val peakProb = peakHour?.precipitationProbability ?: 0
        val peakTime = peakHour?.time?.format(ASSISTANT_HOUR_FORMAT) ?: startHour
        "Önümüzdeki 12 saatlik radar projeksiyonunda $startHour – $endHour arasında yağış kütlesi geçişi bekleniyor (en yüksek ihtimal saat $peakTime civarında %$peakProb)."
    } else {
        val maxProb = peakHour?.precipitationProbability ?: 0
        "Önümüzdeki 12 saat boyunca $cityName merkezinde belirgin bir yağış kütlesi beklenmiyor (maksimum yağış olasılığı %$maxProb)."
    }

    val windInfo = current.windSpeedKmh?.let { "Kütle hareket hızı / yer rüzgârı yaklaşık ${it.roundToInt()} km/sa." }.orEmpty()
    return "$activeEcho $echoWindow $windInfo Harita sekmesindeki '▶ Oynat' butonuyla kütlelerin yönünü harita üzerinde canlı izleyebilirsin."
}

private fun answerWeatherQuestion(question: String, forecast: Forecast?, cityName: String): String {
    val ascii = question.toAsciiSearch()

    // Genel sohbet / yardım soruları (şehir seçilmese bile yanıtlanır)
    if (listOf("ne sorabilirim", "yardim", "neler yapabilirsin", "ozellik", "komut").any { ascii.contains(it) }) {
        return "Bana şunları sorabilirsin:\n" +
            "• 📡 Canlı yağış radarı ve kütle hareketleri ('Radar durumu nasıl?')\n" +
            "• 🚗 Canlı trafik ve yol güvenliği ('Yol ve trafik durumu nasıl?')\n" +
            "• 🕐 Saatlik veya günün belirli vakitleri ('Bu akşam hava nasıl?', 'Saatlik tahmin')\n" +
            "• 📅 Yarın, hafta sonu veya 7 günlük görünüm ('Yarın hava nasıl?', 'Hafta sonu yağmur var mı?', 'Pazartesi nasıl?')\n" +
            "• 👕 Kıyafet ve şemsiye ('Ne giysem?', 'Şemsiye almalı mıyım?')\n" +
            "• 🌿 Hava kalitesi, AQI ve polen ('Hava kalitesi nasıl?', 'Polen alerjim var')\n" +
            "• ☀️ UV endeksi, güneş ve ay ('UV kaç?', 'Gün batımı kaçta?', 'Ay evresi ne?')\n" +
            "• 🏃 Spor ve dış mekan aktivitesi ('Bugün koşu yapılır mı?', 'Kamp için uygun mu?')"
    }

    if (listOf("merhaba", "selam", "iyi gunler", "iyi aksamlar", "gunaydin", "nasilsin", "kimsin").any { ascii == it || ascii.startsWith("$it ") }) {
        return if (forecast == null) {
            "Merhaba! Ben Havadaş Hava Asistanıyım. Canlı yağış radarını hemen sorabilir veya bir şehir seçerek detaylı tahmin, trafik, AQI, polen, UV ve kombin önerisi alabilirsin."
        } else {
            val cond = WeatherCondition.fromWeatherCode(forecast.current.weatherCode)
            "Merhaba! $cityName şu an ${forecast.current.temperatureC.roundToInt()}° ve ${cond.label.lowercase(TR_LOCALE)}. Sana radar, saatlik/haftalık tahmin, trafik, kıyafet veya hava kalitesi konusunda nasıl yardımcı olabilirim?"
        }
    }

    if (ascii.contains("tesekkur") || ascii.contains("sag ol") || ascii.contains("eyvallah")) {
        return "Rica ederim! Başka bir hava, radar veya yol sorusu olursa her zaman buradayım."
    }

    if (forecast == null) {
        return "Bu soruyu şehre özel yanıtlayabilmem için önce bir şehir seçip hava durumu ekranını açmalısın. (Canlı yağış radarı durumunu ise doğrudan 'Radar durumu nasıl?' diyerek sorabilirsin.)"
    }

    val current = forecast.current
    val today = forecast.daily.firstOrNull()
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcomingHours = forecast.hourly.filter { !it.time.isBefore(now) }.ifEmpty { forecast.hourly }
    val rainChance = (upcomingHours.take(6).maxOfOrNull { it.precipitationProbability ?: 0 } ?: 0)
        .coerceAtLeast(today?.precipitationProbability ?: 0)
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val rainyNow = condition in setOf(
        WeatherCondition.DRIZZLE,
        WeatherCondition.RAIN,
        WeatherCondition.RAIN_SHOWERS,
        WeatherCondition.THUNDERSTORM
    )

    // 1. Trafik ve yol durumu
    if (isTrafficQuestion(ascii)) {
        return trafficWeatherOnlyAdvice(cityName, forecast)
    }

    // 2. Yarın soruları
    if (ascii.contains("yarin")) {
        return tomorrowAdvice(cityName, forecast)
    }

    // 3. Hafta sonu soruları
    if (ascii.contains("hafta sonu") || ascii.contains("haftasonu")) {
        return weekendAdvice(cityName, forecast)
    }

    // 4. Belirli bir gün adı (Pazartesi, Salı, Çarşamba, Perşembe, Cuma, Cumartesi, Pazar)
    findAskedDay(ascii, forecast.daily)?.let { matchedDay ->
        return specificDayAdvice(cityName, matchedDay, forecast)
    }

    // 5. 7 Günlük / Haftalık özet / En sıcak / En soğuk gün
    if (listOf("7 gun", "yedi gun", "haftalik", "bu hafta", "hafta boyu", "en sicak", "en soguk").any { ascii.contains(it) }) {
        return weeklySummaryAdvice(cityName, forecast)
    }

    // 6. Saatlik tahmin / Sabah, Öğle, Akşam, Gece
    if (listOf("saatlik", "onumuzdeki saat", "sabah", "ogle", "aksam", "gece", "saat kacta").any { ascii.contains(it) }) {
        return timeOfDayOrHourlyAdvice(ascii, cityName, forecast)
    }

    // 7. Hava kalitesi, AQI ve Polen / Alerji
    if (listOf("hava kalitesi", "aqi", "kirlilik", "polen", "alerji", "astim", "nefes", "toz").any { ascii.contains(it) }) {
        return airQualityAndPollenAdvice(cityName, forecast)
    }

    // 8. UV endeksi ve güneşten korunma
    if (listOf("uv", "ultraviyole", "gunes kremi", "gunes gozlugu", "yanik", "zararli isin").any { ascii.contains(it) }) {
        return uvAdvice(cityName, forecast)
    }

    // 9. Güneş doğumu/batımı ve Ay evresi
    if (listOf("gun dogumu", "gun batimi", "gunes kacta", "batis", "dogus", "iftar", "sahur", "ay ", "ay dogumu", "ay batimi", "dolunay", "hilal", "ay evresi").any { ascii.contains(it) }) {
        return astronomyAdvice(cityName, today)
    }

    // 10. Spor, Koşu, Yürüyüş, Kamp, Piknik, Bisiklet
    if (listOf("kosu", "spor", "yuruyus", "bisiklet", "kamp", "piknik", "antrenman", "egzersiz", "disari cik").any { ascii.contains(it) }) {
        return outdoorActivityAdvice(cityName, forecast)
    }

    // 11. Kıyafet ve kombin ("konbin" yazım hatası dahil)
    if (listOf("giy", "kiyafet", "kombin", "konbin", "mont", "ceket", "tisort", "kazak", "usur", "terler", "elbise", "hirka").any { ascii.contains(it) }) {
        return outfitAdvice(
            feelsLike = current.apparentTemperatureC ?: current.temperatureC,
            rainChance = rainChance,
            windKmh = current.windSpeedKmh,
            uvIndex = forecast.airQuality?.uvIndex ?: current.uvIndex ?: today?.uvIndexMax
        )
    }

    // 12. Şemsiye, Yağmur, Kar, Fırtına
    if (listOf("semsiye", "yagmur", "yagis", "kar", "firtina", "saganak", "islan", "dolu").any { ascii.contains(it) }) {
        return umbrellaAndRainHoursAdvice(rainChance, rainyNow, upcomingHours)
    }

    // 13. Basınç, Çiy noktası, Görüş mesafesi
    if (listOf("basinc", "hpa", "migren", "bas agrisi").any { ascii.contains(it) }) {
        val p = current.pressureHpa
        return if (p != null) {
            val note = when {
                p < 1008 -> "Alçak basınç etkisi var; yağış olasılığı artabilir ve basınç değişimine duyarlı kişilerde baş ağrısı/halsizlik yapabilir."
                p > 1022 -> "Yüksek basınç hakim; genellikle daha durgun ve açık bir atmosfer sağlar."
                else -> "Normal atmosferik basınç aralığında."
            }
            "$cityName için yüzey basıncı şu an ${p.roundToInt()} hPa. $note"
        } else {
            "$cityName için şu anda basınç verisi bulunmuyor."
        }
    }

    if (listOf("ciy", "buhar", "bunaltici").any { ascii.contains(it) }) {
        val dew = current.dewPointC
        return if (dew != null) {
            val comfort = when {
                dew >= 20 -> "Hava oldukça bunaltıcı ve nemli hissedilir."
                dew >= 16 -> "Hafif nemli bir his var."
                dew <= 0 -> "Hava oldukça kuru ve soğuk."
                else -> "Nem konfor açısından ideal seviyede."
            }
            "$cityName için çiy noktası ${dew.roundToInt()}°. $comfort"
        } else {
            "$cityName için çiy noktası verisi mevcut değil."
        }
    }

    if (listOf("gorus", "gorunurluk", "sis").any { ascii.contains(it) }) {
        val visKm = current.visibilityMeters?.let { it / 1000.0 }
        return if (visKm != null) {
            val visNote = when {
                visKm < 1.0 -> "Yoğun sis veya yağış nedeniyle görüş çok düşük; trafikte sis farlarını kullan."
                visKm < 4.0 -> "Görüş mesafesi orta düzeyde (puslu)."
                else -> "Görüş mesafesi açık ve net."
            }
            "$cityName için görüş mesafesi yaklaşık ${String.format(TR_LOCALE, "%.1f", visKm)} km. $visNote"
        } else {
            "$cityName için görüş mesafesi verisi bulunmuyor."
        }
    }

    // 14. Rüzgâr
    if (ascii.contains("ruz") || ascii.contains("esinti") || ascii.contains("lodos") || ascii.contains("poyraz")) {
        val wind = current.windSpeedKmh?.roundToInt() ?: 0
        val scale = when {
            wind < 12 -> "hafif bir esinti var"
            wind < 28 -> "orta kuvvette rüzgâr esiyor"
            wind < 45 -> "kuvvetli rüzgâr var; açık alanda hissedilen sıcaklığı düşürür"
            else -> "fırtına şiddetinde kuvvetli rüzgâr var; çatı uçması ve ağaç devrilmelerine karşı dikkatli olunmalı"
        }
        return "$cityName genelinde rüzgâr şu an yaklaşık $wind km/sa hızında ($scale)."
    }

    // 15. Nem
    if (ascii.contains("nem")) {
        val hum = current.humidityPercent
        return if (hum != null) {
            val note = when {
                hum >= 75 -> "Yüksek nem nedeniyle hava olduğundan daha yoğun hissedilebilir."
                hum <= 30 -> "Hava oldukça kuru; bol su tüketmek iyi olur."
                else -> "Konforlu bir nem aralığında."
            }
            "$cityName için şu anki bağıl nem oranı %$hum. $note"
        } else {
            "Şu anki nem oranı verisi bulunmuyor."
        }
    }

    // 16. Sıcaklık ve genel durum
    if (listOf("sicak", "derece", "hava", "kac", "durum", "bugun").any { ascii.contains(it) }) {
        val minMax = today?.let { " Bugün en düşük ${it.minTemperatureC.roundToInt()}°, en yüksek ${it.maxTemperatureC.roundToInt()}° bekleniyor." }.orEmpty()
        return "$cityName şu an ${condition.label}, sıcaklık ${current.temperatureC.roundToInt()}° (hissedilen ${(current.apparentTemperatureC ?: current.temperatureC).roundToInt()}°).$minMax Önümüzdeki saatlerde maksimum yağış ihtimali %$rainChance."
    }

    // Varsayılan kapsamlı özet
    val minMax = today?.let { "Gün içi aralık: ${it.minTemperatureC.roundToInt()}° / ${it.maxTemperatureC.roundToInt()}°." }.orEmpty()
    return "$cityName şu an ${condition.label} ve ${current.temperatureC.roundToInt()}° (hissedilen ${(current.apparentTemperatureC ?: current.temperatureC).roundToInt()}°). $minMax\n\nBana canlı yağış radarını, yarınki/hafta sonu tahminini, saatlik durumu, trafiği, kıyafet/şemsiye önerisini, hava kalitesini (AQI/polen), UV endeksini veya spor uygunluğunu sorabilirsin."
}

private fun tomorrowAdvice(cityName: String, forecast: Forecast): String {
    val tomorrow = forecast.daily.getOrNull(1)
        ?: return "$cityName için yarının tahmini henüz mevcut değil."
    val cond = WeatherCondition.fromWeatherCode(tomorrow.weatherCode)
    val rainProb = tomorrow.precipitationProbability ?: 0
    val tomorrowHours = forecast.hourly.filter { it.time.toLocalDate() == tomorrow.date }
    val morningTemp = tomorrowHours.firstOrNull { it.time.hour == 9 }?.temperatureC?.roundToInt()
    val afternoonTemp = tomorrowHours.firstOrNull { it.time.hour == 15 }?.temperatureC?.roundToInt()
    val eveningTemp = tomorrowHours.firstOrNull { it.time.hour == 21 }?.temperatureC?.roundToInt()

    val hourlyBreakdown = if (morningTemp != null && afternoonTemp != null && eveningTemp != null) {
        " Sabah 09:00'da $morningTemp°, öğleden sonra 15:00'te $afternoonTemp°, akşam 21:00'de $eveningTemp° civarında olacak."
    } else ""

    val rainNote = when {
        rainProb >= 60 -> " Yağış ihtimali yüksek (%$rainProb); planlarını yaparken şemsiyeni unutma."
        rainProb >= 30 -> " Gün içinde %$rainProb ihtimalle geçiş yağışı görülebilir."
        else -> " Belirgin bir yağış beklenmiyor (%$rainProb)."
    }

    return "📅 Yarın (${tomorrow.date.format(ASSISTANT_DAY_FORMAT)}) $cityName için tahmin: ${cond.emoji} ${cond.label}, en düşük ${tomorrow.minTemperatureC.roundToInt()}°, en yüksek ${tomorrow.maxTemperatureC.roundToInt()}°.$hourlyBreakdown$rainNote"
}

private fun weekendAdvice(cityName: String, forecast: Forecast): String {
    val weekendDays = forecast.daily.filter {
        it.date.dayOfWeek == DayOfWeek.SATURDAY || it.date.dayOfWeek == DayOfWeek.SUNDAY
    }
    if (weekendDays.isEmpty()) {
        return "$cityName için hafta sonu verisi 7 günlük pencerede bulunamadı."
    }
    val lines = weekendDays.joinToString("\n") { day ->
        val dayName = day.date.format(DateTimeFormatter.ofPattern("EEEE", TR_LOCALE)).replaceFirstChar(Char::titlecase)
        val cond = WeatherCondition.fromWeatherCode(day.weatherCode)
        val rain = day.precipitationProbability?.let { " · Yağış %$it" }.orEmpty()
        "• $dayName: ${cond.emoji} ${cond.label}, ${day.minTemperatureC.roundToInt()}° / ${day.maxTemperatureC.roundToInt()}°$rain"
    }
    return "🗓️ $cityName Hafta Sonu Tahmini:\n$lines"
}

private fun findAskedDay(normalized: String, daily: List<DailyForecast>): DailyForecast? {
    val dayMap = mapOf(
        "pazartesi" to DayOfWeek.MONDAY,
        "salı" to DayOfWeek.TUESDAY,
        "sali" to DayOfWeek.TUESDAY,
        "çarşamba" to DayOfWeek.WEDNESDAY,
        "carsamba" to DayOfWeek.WEDNESDAY,
        "perşembe" to DayOfWeek.THURSDAY,
        "persembe" to DayOfWeek.THURSDAY,
        "cuma" to DayOfWeek.FRIDAY,
        "cumartesi" to DayOfWeek.SATURDAY,
        "pazar" to DayOfWeek.SUNDAY
    )
    // "pazartesi" ve "pazar" çakışmasını önlemek için uzun kelimeleri önce kontrol et
    val matchedEntry = dayMap.entries
        .sortedByDescending { it.key.length }
        .firstOrNull { normalized.contains(it.key) }
        ?: return null
    return daily.firstOrNull { it.date.dayOfWeek == matchedEntry.value }
}

private fun specificDayAdvice(cityName: String, day: DailyForecast, forecast: Forecast): String {
    val dayTitle = day.date.format(ASSISTANT_DAY_FORMAT).replaceFirstChar(Char::titlecase)
    val cond = WeatherCondition.fromWeatherCode(day.weatherCode)
    val rain = day.precipitationProbability ?: 0
    val dayHours = forecast.hourly.filter { it.time.toLocalDate() == day.date }
    val peakHour = dayHours.maxByOrNull { it.temperatureC }
    val peakText = peakHour?.let { " Günün en sıcak saati ${it.time.format(ASSISTANT_HOUR_FORMAT)} (${it.temperatureC.roundToInt()}°)." }.orEmpty()
    return "📅 $dayTitle günü $cityName tahmini: ${cond.emoji} ${cond.label}, en düşük ${day.minTemperatureC.roundToInt()}°, en yüksek ${day.maxTemperatureC.roundToInt()}°, yağış olasılığı %$rain.$peakText (7 Günlük tablodan bu güne dokunarak tüm saatlerini inceleyebilirsin.)"
}

private fun weeklySummaryAdvice(cityName: String, forecast: Forecast): String {
    if (forecast.daily.isEmpty()) return "$cityName için 7 günlük veri bulunamadı."
    val warmest = forecast.daily.maxByOrNull { it.maxTemperatureC }
    val coldest = forecast.daily.minByOrNull { it.minTemperatureC }
    val rainyDays = forecast.daily.filter { (it.precipitationProbability ?: 0) >= 45 }

    val warmestText = warmest?.let {
        val name = it.date.format(DateTimeFormatter.ofPattern("EEEE", TR_LOCALE)).replaceFirstChar(Char::titlecase)
        "En sıcak gün $name (${it.maxTemperatureC.roundToInt()}°)"
    }.orEmpty()
    val coldestText = coldest?.let {
        val name = it.date.format(DateTimeFormatter.ofPattern("EEEE", TR_LOCALE)).replaceFirstChar(Char::titlecase)
        "en serin gece/sabah $name (${it.minTemperatureC.roundToInt()}°)"
    }.orEmpty()
    val rainText = if (rainyDays.isEmpty()) {
        "Önümüzdeki 7 gün boyunca yüksek ihtimalli bir yağış görünmüyor."
    } else {
        val names = rainyDays.joinToString(", ") {
            it.date.format(DateTimeFormatter.ofPattern("EEEE", TR_LOCALE)).replaceFirstChar(Char::titlecase)
        }
        "Yağış beklenen günler: $names."
    }
    return "📊 $cityName 7 Günlük Özet: $warmestText, $coldestText. $rainText"
}

private fun timeOfDayOrHourlyAdvice(normalized: String, cityName: String, forecast: Forecast): String {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = forecast.hourly.filter { !it.time.isBefore(now) }.take(12).ifEmpty { forecast.hourly.take(12) }

    val targetHours = when {
        normalized.contains("sabah") -> forecast.hourly.filter { it.time.hour in 6..11 }.take(4)
        normalized.contains("ogle") -> forecast.hourly.filter { it.time.hour in 12..16 }.take(4)
        normalized.contains("aksam") -> forecast.hourly.filter { it.time.hour in 18..22 }.take(4)
        normalized.contains("gece") -> forecast.hourly.filter { it.time.hour in 0..5 || it.time.hour == 23 }.take(4)
        else -> upcoming.take(6)
    }.ifEmpty { upcoming.take(5) }

    val summary = targetHours.joinToString(" · ") { h ->
        val cond = WeatherCondition.fromWeatherCode(h.weatherCode)
        val rain = h.precipitationProbability?.takeIf { it >= 25 }?.let { " (%$it)" }.orEmpty()
        "${h.time.format(ASSISTANT_HOUR_FORMAT)} ${cond.emoji}${h.temperatureC.roundToInt()}°$rain"
    }
    return "🕐 $cityName saatlik görünüm:\n$summary"
}

private fun airQualityAndPollenAdvice(cityName: String, forecast: Forecast): String {
    val air = forecast.airQuality
    val aqi = air?.europeanAqi
    val pollen = air?.pollenGrainsPerCubicMeter

    val aqiPart = if (aqi != null) {
        val status = when {
            aqi <= 20 -> "İyi (temiz hava)"
            aqi <= 40 -> "Makul"
            aqi <= 60 -> "Orta (hassas gruplar dikkat etmeli)"
            aqi <= 80 -> "Kötü (açık hava eforunu azalt)"
            else -> "Çok yüksek kirlilik (dışarıda uzun süre kalma)"
        }
        "Avrupa Hava Kalitesi Endeksi (AQI): $aqi — $status."
    } else {
        "Anlık AQI ölçümü şu an mevcut değil."
    }

    val pollenPart = if (pollen != null) {
        val level = when {
            pollen < 10 -> "Düşük"
            pollen < 50 -> "Orta (alerjik bünyeler dikkatli olmalı)"
            else -> "Yüksek (polen alerjisi olanlar maske/gözlük kullanmalı)"
        }
        "Polen yoğunluğu: ${pollen.roundToInt()} tane/m³ ($level)."
    } else {
        "Polen yoğunluğu mevsim dışı veya düşük seviyede."
    }

    return "🌿 $cityName Hava Kalitesi & Polen:\n$aqiPart $pollenPart"
}

private fun uvAdvice(cityName: String, forecast: Forecast): String {
    val uv = forecast.airQuality?.uvIndex
        ?: forecast.current.uvIndex
        ?: forecast.daily.firstOrNull()?.uvIndexMax
    val maxUv = forecast.daily.firstOrNull()?.uvIndexMax ?: uv

    if (uv == null && maxUv == null) {
        return "$cityName için şu anda UV endeksi verisi bulunmuyor."
    }
    val value = uv ?: maxUv ?: 0.0
    val protection = when {
        value < 3.0 -> "Risk düşük; ekstra güneş koruması gerekmiyor."
        value < 6.0 -> "Orta seviye; öğle saatlerinde güneş gözlüğü ve SPF 30+ güneş kremi önerilir."
        value < 8.0 -> "Yüksek UV; 11:00–16:00 arası doğrudan güneşten kaçın, şapka ve güneş kremi kullan."
        else -> "Çok yüksek/aşırı UV; öğle saatlerinde gölgede kalmaya özen göster."
    }
    val maxText = maxUv?.let { " (Gün içi maksimum UV: ${String.format(TR_LOCALE, "%.1f", it)})" }.orEmpty()
    return "☀️ $cityName UV Endeksi: ${String.format(TR_LOCALE, "%.1f", value)}$maxText. $protection"
}

private fun astronomyAdvice(cityName: String, today: DailyForecast?): String {
    if (today == null) return "$cityName için astronomi verisi bulunamadı."
    val sunrise = today.sunrise?.format(ASSISTANT_HOUR_FORMAT) ?: "—"
    val sunset = today.sunset?.format(ASSISTANT_HOUR_FORMAT) ?: "—"
    val daylight = if (today.sunrise != null && today.sunset != null && today.sunset.isAfter(today.sunrise)) {
        val minutes = ChronoUnit.MINUTES.between(today.sunrise, today.sunset)
        " (Gün uzunluğu: ${minutes / 60} sa ${minutes % 60} dk)"
    } else ""
    val moonPhase = today.moonPhase?.let { phase ->
        when {
            phase < 0.03 || phase >= 0.97 -> "Yeni ay 🌑"
            phase < 0.25 -> "Hilal 🌒"
            phase < 0.28 -> "İlk dördün 🌓"
            phase < 0.5 -> "Şişkin ay 🌔"
            phase < 0.53 -> "Dolunay 🌕"
            phase < 0.75 -> "Küçülen ay 🌖"
            phase < 0.78 -> "Son dördün 🌗"
            else -> "Hilal 🌘"
        }
    } ?: "Belirsiz"
    return "🌅 $cityName Astronomi Bilgisi:\n• Gün doğumu: $sunrise · Gün batımı: $sunset$daylight\n• Ay evresi: $moonPhase"
}

private fun outdoorActivityAdvice(cityName: String, forecast: Forecast): String {
    val current = forecast.current
    val aqi = forecast.airQuality?.europeanAqi ?: 20
    val pollen = forecast.airQuality?.pollenGrainsPerCubicMeter ?: 0.0
    val wind = current.windSpeedKmh ?: 0.0
    val temp = current.apparentTemperatureC ?: current.temperatureC
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val next6Hours = forecast.hourly.filter { !it.time.isBefore(now) }.take(6)
    val rainRisk = next6Hours.maxOfOrNull { it.precipitationProbability ?: 0 } ?: 0

    val issues = buildList {
        if (rainRisk >= 55) add("yağış ihtimali yüksek (%$rainRisk)")
        if (aqi >= 60) add("hava kalitesi orta/kötü (AQI $aqi)")
        if (pollen >= 60.0) add("polen seviyesi yüksek")
        if (wind >= 38.0) add("rüzgâr kuvvetli (${wind.roundToInt()} km/sa)")
        if (temp <= 2.0) add("hava oldukça soğuk (${temp.roundToInt()}°)")
        if (temp >= 33.0) add("sıcaklık çok yüksek (${temp.roundToInt()}°)")
    }

    val bestHour = forecast.hourly
        .filter { !it.time.isBefore(now) && it.time.hour in 7..20 }
        .take(12)
        .minByOrNull { (it.precipitationProbability ?: 0) * 2 + kotlin.math.abs(it.temperatureC - 18.0).roundToInt() }

    val bestHourText = bestHour?.let {
        " Bugün dış mekan için en ideal saat: ${it.time.format(ASSISTANT_HOUR_FORMAT)} (${it.temperatureC.roundToInt()}°, yağış %${it.precipitationProbability ?: 0})."
    }.orEmpty()

    return if (issues.isEmpty()) {
        "🏃 $cityName şu an koşu, yürüyüş ve açık hava sporları için oldukça uygun! Hissedilen sıcaklık ${temp.roundToInt()}°, rüzgâr ${wind.roundToInt()} km/sa.$bestHourText"
    } else {
        "🏃 $cityName için dış mekan aktivite uyarısı: ${issues.joinToString(", ")}.$bestHourText"
    }
}

private fun weatherRoadSummary(forecast: Forecast): String {
    val current = forecast.current
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val rainChance = forecast.hourly.take(6).maxOfOrNull { it.precipitationProbability ?: 0 } ?: 0
    val hazards = buildList {
        when (condition) {
            WeatherCondition.FOG -> add("sis var")
            WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> add("yağış nedeniyle yollar ıslak olabilir")
            WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> add("kar ve buzlanma riski olabilir")
            WeatherCondition.THUNDERSTORM -> add("fırtına ve kuvvetli yağış görülebilir")
            else -> Unit
        }
        if (current.visibilityMeters != null && current.visibilityMeters < 1_000) add("görüş mesafesi düşük")
        if ((current.windSpeedKmh ?: 0.0) >= 45.0) add("rüzgâr kuvvetli")
        if (rainChance >= 60 && condition !in setOf(WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS, WeatherCondition.THUNDERSTORM)) add("önümüzdeki saatlerde yağış ihtimali %$rainChance")
    }
    return if (hazards.isEmpty()) "Hava tahmininde belirgin yağış, kar veya sis riski görünmüyor."
    else "Hava kaynaklı dikkat noktaları: ${hazards.joinToString(", ")}."
}

private fun trafficWeatherOnlyAdvice(cityName: String, forecast: Forecast) =
    "Canlı trafik servisi henüz yapılandırılmamış; $cityName için trafik yoğunluğu veya kaza bilgisini doğrulayamam. ${weatherRoadSummary(forecast)}"

private fun answerTraffic(cityName: String, traffic: com.kampplus.hava.data.TrafficStatus, forecast: Forecast): String {
    val relativeSpeed = if (traffic.freeFlowSpeedKmh > 0) traffic.currentSpeedKmh.toDouble() / traffic.freeFlowSpeedKmh else 1.0
    val state = when {
        traffic.roadClosed -> "yol kapalı görünüyor"
        relativeSpeed >= 0.8 -> "akıcı"
        relativeSpeed >= 0.6 -> "orta yoğunlukta"
        relativeSpeed >= 0.3 -> "yoğun"
        else -> "çok yoğun"
    }
    val currentTime = traffic.currentTravelTimeSeconds
    val freeFlowTime = traffic.freeFlowTravelTimeSeconds
    val delay = if (currentTime != null && freeFlowTime != null && currentTime > freeFlowTime) {
        " Serbest akışa göre yaklaşık ${(currentTime - freeFlowTime) / 60} dakika gecikme var."
    } else ""
    val confidence = traffic.confidence?.let { " Veri güveni: ${(it * 100).toInt()}%." }.orEmpty()
    return "$cityName çevresindeki en yakın yol segmentinde trafik $state; mevcut hız ${traffic.currentSpeedKmh} km/sa, serbest akış hızı ${traffic.freeFlowSpeedKmh} km/sa.$delay$confidence ${weatherRoadSummary(forecast)} Bu ölçüm seçilen koordinata yakın tek bir yol segmentini kapsar."
}

private fun outfitAdvice(
    feelsLike: Double,
    rainChance: Int = 0,
    windKmh: Double? = null,
    uvIndex: Double? = null
): String {
    val base = when {
        feelsLike <= 0 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Kalın kışlık mont, bere, atkı ve eldiven tercih et."
        feelsLike <= 8 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Sıcak tutan bir mont, kazak ve kapalı ayakkabı iyi olur."
        feelsLike <= 15 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Ceket, trençkot veya ince mont al; katmanlı giyinmek rahat ettirir."
        feelsLike <= 22 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Uzun kollu hafif bir üst, gömlek veya ince hırka yeterli olabilir."
        feelsLike < 29 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Tişört ve hafif, nefes alan kıyafetler seç."
        else -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. İnce, açık renkli pamuklu kıyafetler giy; bol su içmeyi unutma."
    }
    val extras = buildList {
        if (rainChance >= 45) add("Yağış ihtimali %$rainChance olduğu için su geçirmez dış katman veya şemsiye al.")
        if ((windKmh ?: 0.0) >= 30.0) add("Rüzgâr (${windKmh!!.roundToInt()} km/sa) nedeniyle rüzgârlık faydalı olur.")
        if ((uvIndex ?: 0.0) >= 5.0) add("UV endeksi yüksek; güneş gözlüğü ve şapka kullan.")
    }
    return if (extras.isEmpty()) base else "$base ${extras.joinToString(" ")}"
}

private fun umbrellaAndRainHoursAdvice(
    rainChance: Int,
    rainyNow: Boolean,
    upcomingHours: List<com.kampplus.hava.model.HourlyForecast>
): String {
    val rainySlot = upcomingHours.take(12).filter { (it.precipitationProbability ?: 0) >= 40 }
    val slotNote = if (rainySlot.isNotEmpty()) {
        val hoursText = rainySlot.take(4).joinToString(", ") {
            "${it.time.format(ASSISTANT_HOUR_FORMAT)} (%${it.precipitationProbability})"
        }
        " Özellikle şu saatlerde yağış riski öne çıkıyor: $hoursText."
    } else ""

    return when {
        rainyNow -> "☂️ Şu an yağış var; dışarı çıkarken şemsiyeni mutlaka yanına al.$slotNote"
        rainChance >= 50 -> "☂️ Önümüzdeki saatlerde yağış ihtimali %$rainChance. Şemsiyeni alman iyi olur.$slotNote"
        rainChance >= 30 -> "🌂 Yağış ihtimali %$rainChance. Çantanda katlanır küçük bir şemsiye bulundurmak işine yarayabilir.$slotNote"
        else -> "☀️ Yağış ihtimali düşük (%$rainChance); bugün şemsiyeye pek gerek görünmüyor."
    }
}
