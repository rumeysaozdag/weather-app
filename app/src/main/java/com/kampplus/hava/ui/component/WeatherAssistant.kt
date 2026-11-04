package com.kampplus.hava.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.theme.HavaTheme
import java.util.Locale
import kotlin.math.roundToInt

private data class ChatMessage(val text: String, val fromUser: Boolean)

/** Small floating bot button and a local, forecast-aware weather chat. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WeatherAssistantButton(
    forecast: Forecast?,
    cityName: String = "",
    modifier: Modifier = Modifier
) {
    var isChatOpen by rememberSaveable { mutableStateOf(false) }
    SmallFloatingActionButton(
        onClick = { isChatOpen = true },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text("🤖", style = MaterialTheme.typography.titleLarge)
    }

    if (isChatOpen) {
        HavaTheme(darkTheme = true) {
            ModalBottomSheet(
                onDismissRequest = { isChatOpen = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                AssistantChat(forecast = forecast, cityName = cityName)
            }
        }
    }
}

@Composable
private fun AssistantChat(forecast: Forecast?, cityName: String) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                if (forecast == null) {
                    "Merhaba! Hava durumu hakkında yardımcı olabilirim. Bir şehrin tahminini açınca kıyafet ve şemsiye önerisi de verebilirim."
                } else {
                    "Merhaba! $cityName için tahminleri inceliyorum. Bugün ne giymen gerektiğini, yağmur ihtimalini veya sıcaklığı sorabilirsin."
                },
                fromUser = false
            )
        )
    }
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 420.dp, max = 680.dp).padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("🤖", style = MaterialTheme.typography.headlineMedium)
            Column {
                Text("Hava Asistanı", style = MaterialTheme.typography.titleLarge)
                Text(
                    if (forecast == null) "Şehir seçince tahminine göre yanıtlarım" else cityName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { sendMessage("Bugün ne giysem?", forecast, messages) }, label = { Text("Ne giysem?") })
            AssistChip(onClick = { sendMessage("Şemsiye almalı mıyım?", forecast, messages) }, label = { Text("Şemsiye?") })
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(messages) { _, message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (message.fromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (message.fromUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(0.86f)
                    ) {
                        Text(message.text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Bir şey sor…") },
                shape = RoundedCornerShape(18.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    sendMessage(input, forecast, messages)
                    input = ""
                })
            )
            Button(onClick = {
                sendMessage(input, forecast, messages)
                input = ""
            }, enabled = input.isNotBlank()) {
                Text("Gönder")
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

private fun sendMessage(question: String, forecast: Forecast?, messages: MutableList<ChatMessage>) {
    val cleanedQuestion = question.trim()
    if (cleanedQuestion.isEmpty()) return
    messages += ChatMessage(cleanedQuestion, fromUser = true)
    messages += ChatMessage(answerWeatherQuestion(cleanedQuestion, forecast), fromUser = false)
}

private fun answerWeatherQuestion(question: String, forecast: Forecast?): String {
    if (forecast == null) {
        return "Bu yanıtı şehir tahminine göre verebilmem için önce bir şehir seçip hava durumu ekranını aç."
    }

    val normalized = question.lowercase(Locale.forLanguageTag("tr-TR"))
    val current = forecast.current
    val rainChance = (forecast.hourly.take(6).maxOfOrNull { it.precipitationProbability ?: 0 } ?: 0)
        .coerceAtLeast(forecast.daily.firstOrNull()?.precipitationProbability ?: 0)
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val rainyNow = condition in setOf(
        WeatherCondition.DRIZZLE,
        WeatherCondition.RAIN,
        WeatherCondition.RAIN_SHOWERS,
        WeatherCondition.THUNDERSTORM
    )

    return when {
        normalized.contains("giy") || normalized.contains("kıyafet") || normalized.contains("kombin") -> outfitAdvice(current.apparentTemperatureC ?: current.temperatureC)
        normalized.contains("şemsiye") || normalized.contains("yağmur") || normalized.contains("yağış") -> umbrellaAdvice(rainChance, rainyNow)
        normalized.contains("rüz") -> "Rüzgâr şu an yaklaşık ${current.windSpeedKmh?.roundToInt() ?: 0} km/sa."
        normalized.contains("nem") -> "Şu anki nem oranı ${current.humidityPercent?.let { "%$it" } ?: "veri yok"}."
        normalized.contains("sıcak") || normalized.contains("derece") || normalized.contains("hava") ->
            "${condition.label}. Sıcaklık ${current.temperatureC.roundToInt()}°, hissedilen ${current.apparentTemperatureC?.roundToInt() ?: current.temperatureC.roundToInt()}°."
        else -> "Şu an ${condition.label} ve sıcaklık ${current.temperatureC.roundToInt()}°. Kıyafet, şemsiye, yağış, rüzgâr veya sıcaklık hakkında sorabilirsin."
    }
}

private fun outfitAdvice(feelsLike: Double): String = when {
    feelsLike <= 0 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Kalın mont, atkı ve eldiven tercih et."
    feelsLike <= 8 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Sıcak tutan bir mont ve kapalı ayakkabı iyi olur."
    feelsLike <= 15 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Ceket veya ince mont al; katmanlı giyinmek rahat ettirir."
    feelsLike <= 22 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Uzun kollu hafif bir üst veya ince hırka yeterli olabilir."
    feelsLike < 29 -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. Hafif ve rahat kıyafetler seç."
    else -> "Hissedilen sıcaklık ${feelsLike.roundToInt()}°. İnce, açık renkli kıyafetler giy; güneşten korunup su içmeyi unutma."
}

private fun umbrellaAdvice(rainChance: Int, rainyNow: Boolean): String = when {
    rainyNow -> "Şu an yağış var; dışarı çıkarken şemsiyeni yanına al."
    rainChance >= 50 -> "Önümüzdeki saatlerde yağış ihtimali %$rainChance. Şemsiyeni alman iyi olur."
    rainChance >= 30 -> "Yağış ihtimali %$rainChance. Küçük bir şemsiye işine yarayabilir."
    else -> "Yağış ihtimali düşük (%$rainChance); şemsiyeye pek gerek görünmüyor."
}
