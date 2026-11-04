package com.kampplus.hava.ui.route

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.WeatherAssistantButton
import com.kampplus.hava.ui.theme.HavaTheme
import java.time.format.DateTimeFormatter

@Composable
fun RouteAdviceRoute(
    favoriteCities: List<City>,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouteAdviceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCityId by rememberSaveable { mutableLongStateOf(favoriteCities.firstOrNull()?.id ?: -1L) }
    val selectedCity = favoriteCities.firstOrNull { it.id == selectedCityId } ?: favoriteCities.firstOrNull()

    LaunchedEffect(selectedCity?.id) {
        selectedCity?.let(viewModel::load)
    }

    RouteAdviceScreen(
        cities = favoriteCities,
        selectedCity = selectedCity,
        uiState = uiState,
        onCitySelected = { selectedCityId = it.id },
        onOpenSearch = onOpenSearch,
        onRetry = { selectedCity?.let(viewModel::retry) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteAdviceScreen(
    cities: List<City>,
    selectedCity: City?,
    uiState: LoadState<Forecast>,
    onCitySelected: (City) -> Unit,
    onOpenSearch: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF688597), Color(0xFF405F78), Color(0xFF203650)))
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            floatingActionButton = {
                WeatherAssistantButton(
                    forecast = (uiState as? LoadState.Success)?.data,
                    cityName = selectedCity?.name.orEmpty()
                )
            },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Yol önerisi", color = Color.White)
                            Text("Yağışa göre yol planla", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.13f),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("🌧️", style = MaterialTheme.typography.displaySmall)
                        Column {
                            Text("Yola çıkmadan önce", style = MaterialTheme.typography.titleMedium)
                            Text("Önümüzdeki saatlerin yağışını karşılaştır, daha uygun zamanı seç.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                if (cities.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Önce bir şehir ekle", style = MaterialTheme.typography.titleMedium)
                            Text("Şehirlerim sekmesinden bir şehri favorile. Yol önerisini o şehrin yağış tahminine göre hazırlayalım.", textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.8f))
                            Button(onClick = onOpenSearch) { Text("Şehir ara") }
                        }
                    }
                } else {
                    Text("Tahmin alınacak şehir", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cities.forEach { city ->
                            FilterChip(
                                selected = city.id == selectedCity?.id,
                                onClick = { onCitySelected(city) },
                                label = { Text(city.name) }
                            )
                        }
                    }

                    when (uiState) {
                        LoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                        LoadState.Empty -> Text("Bu şehir için yağış tahmini bulunamadı.", color = Color.White)
                        is LoadState.Error -> HavaTheme(darkTheme = true) { ErrorView(uiState.message, onRetry = onRetry) }
                        is LoadState.Success -> RouteForecastAdvice(city = selectedCity, forecast = uiState.data)
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteForecastAdvice(city: City?, forecast: Forecast) {
    val upcoming = forecast.hourly.take(6)
    val probabilities = upcoming.map { it.precipitationProbability ?: 0 }
    val highestChance = probabilities.maxOrNull() ?: 0
    val bestHour = upcoming.minByOrNull { it.precipitationProbability ?: 0 }
    val bestTime = bestHour?.time?.format(DateTimeFormatter.ofPattern("HH:mm"))
    val recommendation = when {
        highestChance >= 70 -> "Önümüzdeki saatlerde yağış kuvvetlenebilir. Mümkünse ${bestTime ?: "yağışın düşük olduğu saatte"} yola çık; kapalı veya toplu taşıma seçeneğini değerlendir."
        highestChance >= 40 -> "Yağış ihtimali artıyor. ${bestTime ?: "Daha kuru olan saatleri"} tercih etmek ve yola biraz erken çıkmak iyi olabilir."
        else -> "Önümüzdeki saatlerde yağış ihtimali düşük. ${bestTime?.let { "$it civarı" } ?: "Uygun gördüğün saatte"} yola çıkabilirsin."
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AdviceCard(
            title = "${city?.name ?: "Şehrin"} için öneri",
            message = recommendation,
            icon = if (highestChance >= 60) "☔" else "🛣️"
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Saatlik yağış", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
            Text("En yüksek: %$highestChance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.78f))
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            upcoming.forEach { hour ->
                val chance = hour.precipitationProbability ?: 0
                Surface(
                    modifier = Modifier.size(width = 72.dp, height = 92.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.13f),
                    contentColor = Color.White
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(hour.time.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(5.dp))
                        Text("${chance}%", style = MaterialTheme.typography.titleMedium)
                        Text("yağış", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f))
                    }
                }
            }
        }
        AdviceCard(
            title = "Yol ipucu",
            message = if (highestChance >= 50) {
                "Islak zeminde takip mesafeni artır, ani frenlerden kaçın ve su birikintilerine dikkat et."
            } else {
                "Yağış düşük görünse de yola çıkmadan önce güncel trafik ve hava koşullarını kontrol et."
            },
            icon = "💡"
        )
        Text(
            "Bu öneri hava tahminine dayanır; canlı trafik veya harita rotası içermez.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun AdviceCard(title: String, message: String, icon: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.14f),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(message, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.84f))
            }
        }
    }
}
