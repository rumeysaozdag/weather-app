package com.kampplus.hava.ui.city

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.ShimmerList
import com.kampplus.hava.ui.component.WeatherAssistantButton
import com.kampplus.hava.ui.theme.HavaTheme

/**
 * Şehir arama ekranının "bağlayıcı" katmanı.
 *
 * Tek işi ViewModel'i edinip state'i toplamak ve [CityScreen]'e aktarmaktır.
 * Bu ayrım sayesinde [CityScreen] ViewModel tanımaz: @Preview'da ve testte
 * elle state verilerek çalıştırılabilir.
 *
 * `collectAsStateWithLifecycle`: StateFlow'u Compose state'ine
 * çevirir ve uygulama arka plandayken dinlemeyi durdurur.
 */
@Composable
fun CityRoute(
    onCityClick: (City) -> Unit,
    favoriteCities: List<City> = emptyList(),
    onToggleFavorite: (City) -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CityViewModel = viewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCityId by viewModel.selectedCityId.collectAsStateWithLifecycle()

    CityScreen(
        query = query,
        uiState = uiState,
        selectedCityId = selectedCityId,
        favoriteCities = favoriteCities,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onCityClick = onCityClick,
        onCitySelected = viewModel::selectCity,
        onToggleFavorite = onToggleFavorite,
        onOpenFavorites = onOpenFavorites,
        onToggleTheme = onToggleTheme,
        modifier = modifier
    )
}

/**
 * Şehir arama ekranı: `when` ile her duruma bir görünüm.
 *
 * State tutmaz, yalnızca aldığı değerleri çizer ve olayları yukarı bildirir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    query: String,
    uiState: LoadState<List<City>>,
    selectedCityId: Long? = null,
    favoriteCities: List<City> = emptyList(),
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCityClick: (City) -> Unit,
    onCitySelected: (City) -> Unit = {},
    onToggleFavorite: (City) -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF688597),
                        Color(0xFF405F78),
                        Color(0xFF203650)
                    )
                )
            )
    ) {
      Box(
          modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 110.dp, y = 70.dp)
              .size(260.dp)
              .background(
                  Brush.radialGradient(
                      colors = listOf(Color(0xFFB2E7EB).copy(alpha = 0.22f), Color.Transparent)
                  ),
                  CircleShape
              )
      )
      Box(
          modifier = Modifier
              .align(Alignment.BottomStart)
              .offset(x = (-130).dp, y = 100.dp)
              .size(300.dp)
              .background(
                  Brush.radialGradient(
                      colors = listOf(Color(0xFFB5C7FF).copy(alpha = 0.14f), Color.Transparent)
                  ),
                  CircleShape
              )
      )
      Text("☁️", modifier = Modifier.align(Alignment.TopEnd).offset(x = 22.dp, y = 126.dp).alpha(0.12f), style = MaterialTheme.typography.displayLarge)
      Text("☁️", modifier = Modifier.align(Alignment.BottomStart).offset(x = (-28).dp, y = 2.dp).alpha(0.09f), style = MaterialTheme.typography.displayLarge)
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentColor = Color.White,
        floatingActionButton = {
            WeatherAssistantButton(forecast = null)
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("☀", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Column {
                            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("HAVA DURUMU", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f))
                        }
                    }
                },
                actions = {
                    val themeActionLabel = stringResource(R.string.action_toggle_theme)
                    IconButton(onClick = onOpenFavorites) {
                        Text(
                            text = "★",
                            modifier = Modifier.semantics { contentDescription = "Şehirlerim" },
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    IconButton(onClick = onToggleTheme) {
                        Text(
                            text = "☾",
                            modifier = Modifier.semantics {
                                contentDescription = themeActionLabel
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
      ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(innerPadding)
        ) {
            WelcomeCard()
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
            )
            val resultCount = (uiState as? LoadState.Success)?.data?.size ?: 0
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ŞEHİRLER", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.82f), modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = Color(0xFFFFD4AE)) {
                    Text(
                        text = stringResource(R.string.city_result_count, resultCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF3A4054),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Dört durumun tamamı burada karşılanır. `sealed` olduğu için
                // bir dalı unutursak Kotlin derleme hatası verir.
                when (uiState) {
                    LoadState.Loading -> HavaTheme(darkTheme = true) { ShimmerList() }
                    is LoadState.Success -> CityList(
                        cities = uiState.data,
                        selectedCityId = selectedCityId,
                        favoriteIds = favoriteCities.mapTo(mutableSetOf()) { it.id },
                        onToggleFavorite = onToggleFavorite,
                        onCityClick = { city ->
                            onCitySelected(city)
                            onCityClick(city)
                        }
                    )
                    LoadState.Empty -> HavaTheme(darkTheme = true) {
                        EmptyView(
                            title = "Şehir bulunamadı",
                            message = "Yazımı kontrol et ya da başka bir şehir dene."
                        )
                    }
                    is LoadState.Error -> HavaTheme(darkTheme = true) {
                        ErrorView(message = uiState.message, onRetry = onSearch)
                    }
                }
            }
        }
      }
    }
}

@Composable
private fun WelcomeCard() {
    val transition = rememberInfiniteTransition(label = "weather-icon-float")
    val floatY = transition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(tween(1_800), repeatMode = RepeatMode.Reverse),
        label = "weather-icon-y"
    ).value
    val rotation = transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2_600), repeatMode = RepeatMode.Reverse),
        label = "weather-icon-rotation"
    ).value

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(500)) + slideInVertically(
            initialOffsetY = { it / 5 },
            animationSpec = tween(500)
        )
    ) {
      Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF68AAB8).copy(alpha = 0.64f), Color(0xFF807399).copy(alpha = 0.56f))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.72f)) {
                Text("Havan nasıl?", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Bir şehir seç, günün tahminine göz at.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f))
            }
            Text(
                "🌤️",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.align(Alignment.CenterEnd).offset(y = floatY.dp).rotate(rotation)
            )
        }
      }
    }
}

/**
 * Arama kutusu.
 *
 * State tutmaz: değeri yukarıdan alır, her değişikliği yukarıya bildirir
 * ("değer aşağı, olay yukarı" — state hoisting).
 */
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        label = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            // Temizle düğmesi yalnızca kutu doluyken görünür.
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
            focusedContainerColor = Color.White.copy(alpha = 0.15f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.34f),
            focusedBorderColor = Color.White.copy(alpha = 0.78f),
            unfocusedLabelColor = Color.White.copy(alpha = 0.74f),
            focusedLabelColor = Color.White,
            unfocusedLeadingIconColor = Color.White.copy(alpha = 0.82f),
            focusedLeadingIconColor = Color.White,
            unfocusedTrailingIconColor = Color.White.copy(alpha = 0.8f),
            focusedTrailingIconColor = Color.White,
            cursorColor = Color.White
        ),
        // Klavyedeki Enter tuşu "Ara" olur.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        )
    )
}

/**
 * Kaydırılabilir şehir listesi.
 *
 * `LazyColumn` tembeldir: yüzlerce şehir olsa da yalnızca ekranda görünen satırlar
 * çizilir, kaybolanlar geri dönüştürülür.
 *
 * `key = { it.id }`: listedeki öğeler değiştiğinde Compose hangi satırın hangisi
 * olduğunu bilir; gereksiz yeniden çizim yapmaz.
 */
@Composable
private fun CityList(
    cities: List<City>,
    selectedCityId: Long?,
    favoriteIds: Set<Long>,
    onToggleFavorite: (City) -> Unit,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = cities, key = { it.id }) { city ->
            CityCard(
                city = city,
                selected = city.id == selectedCityId,
                isFavorite = city.id in favoriteIds,
                onFavoriteClick = { onToggleFavorite(city) },
                onClick = { onCityClick(city) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenPreview() {
    HavaTheme {
        CityScreen(
            query = "Ankara",
            uiState = LoadState.Success(
                listOf(
                    City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara"),
                    City(738648, "Ankara", 41.0050, 39.7269, "TR", "Türkiye", "Trabzon")
                )
            ),
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenEmptyPreview() {
    HavaTheme {
        CityScreen(
            query = "xyz",
            uiState = LoadState.Empty,
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}
