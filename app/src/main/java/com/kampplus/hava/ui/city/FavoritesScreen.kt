package com.kampplus.hava.ui.city

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.theme.HavaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    cities: List<City>,
    onCityClick: (City) -> Unit,
    onRemoveFavorite: (City) -> Unit,
    onBack: () -> Unit,
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
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column {
                        Text("Şehirlerim", color = Color.White)
                        Text("Kaydettiğin yerler", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                }
            )
        }
      ) { innerPadding ->
        if (cities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                HavaTheme(darkTheme = true) {
                    EmptyView(
                        title = "Henüz favori şehrin yok",
                        message = "Şehir aramasındaki yıldız simgesine dokunarak şehirlerini buraya ekleyebilirsin."
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
                Text(
                    text = "Hızlıca hava durumuna bakmak istediğin şehirler",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cities, key = { it.id }) { city ->
                        CityCard(
                            city = city,
                            isFavorite = true,
                            onFavoriteClick = { onRemoveFavorite(city) },
                            onClick = { onCityClick(city) }
                        )
                    }
                }
            }
        }
      }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    HavaTheme {
        FavoritesScreen(
            cities = listOf(City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara")),
            onCityClick = {},
            onRemoveFavorite = {},
            onBack = {}
        )
    }
}
