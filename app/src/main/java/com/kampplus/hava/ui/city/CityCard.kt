
package com.kampplus.hava.ui.city

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.theme.HavaTheme

/**
 * Listedeki tek bir şehir kartı.
 * Türkiye'deki şehirler için plaka kodu, diğer şehirler için ülke kodu gösterir.
 */
@Composable
fun CityCard(
    city: City,
    selected: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.105f),
        label = "city-card-selection"
    ).value
    val cardElevation = animateDpAsState(
        targetValue = if (selected) 5.dp else 1.dp,
        label = "city-card-elevation"
    ).value
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CountryBadge(code = cityBadgeCode(city), isTurkishCity = city.countryCode == "TR")

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onFavoriteClick) {
                Text(
                    text = if (isFavorite) "★" else "☆",
                    color = if (isFavorite) Color(0xFFFFD18A) else Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.78f)
            )
        }
    }
}

/**
 * Türkiye'deki iller için plaka kodunu döndürür.
 * Türkiye dışındaki şehirler için ülke kodunu gösterir.
 */
private fun cityBadgeCode(city: City): String {
    if (city.countryCode != "TR") {
        return city.countryCode ?: "?"
    }

    val plates = mapOf(
        "Adana" to "01",
        "Adıyaman" to "02",
        "Afyonkarahisar" to "03",
        "Ağrı" to "04",
        "Amasya" to "05",
        "Ankara" to "06",
        "Antalya" to "07",
        "Artvin" to "08",
        "Aydın" to "09",
        "Balıkesir" to "10",
        "Bilecik" to "11",
        "Bingöl" to "12",
        "Bitlis" to "13",
        "Bolu" to "14",
        "Burdur" to "15",
        "Bursa" to "16",
        "Çanakkale" to "17",
        "Çankırı" to "18",
        "Çorum" to "19",
        "Denizli" to "20",
        "Diyarbakır" to "21",
        "Edirne" to "22",
        "Elazığ" to "23",
        "Erzincan" to "24",
        "Erzurum" to "25",
        "Eskişehir" to "26",
        "Gaziantep" to "27",
        "Giresun" to "28",
        "Gümüşhane" to "29",
        "Hakkari" to "30",
        "Hatay" to "31",
        "Isparta" to "32",
        "Mersin" to "33",
        "İstanbul" to "34",
        "İzmir" to "35",
        "Kars" to "36",
        "Kastamonu" to "37",
        "Kayseri" to "38",
        "Kırklareli" to "39",
        "Kırşehir" to "40",
        "Kocaeli" to "41",
        "Konya" to "42",
        "Kütahya" to "43",
        "Malatya" to "44",
        "Manisa" to "45",
        "Kahramanmaraş" to "46",
        "Mardin" to "47",
        "Muğla" to "48",
        "Muş" to "49",
        "Nevşehir" to "50",
        "Niğde" to "51",
        "Ordu" to "52",
        "Rize" to "53",
        "Sakarya" to "54",
        "Samsun" to "55",
        "Siirt" to "56",
        "Sinop" to "57",
        "Sivas" to "58",
        "Tekirdağ" to "59",
        "Tokat" to "60",
        "Trabzon" to "61",
        "Tunceli" to "62",
        "Şanlıurfa" to "63",
        "Uşak" to "64",
        "Van" to "65",
        "Yozgat" to "66",
        "Zonguldak" to "67",
        "Aksaray" to "68",
        "Bayburt" to "69",
        "Karaman" to "70",
        "Kırıkkale" to "71",
        "Batman" to "72",
        "Şırnak" to "73",
        "Bartın" to "74",
        "Ardahan" to "75",
        "Iğdır" to "76",
        "Yalova" to "77",
        "Karabük" to "78",
        "Kilis" to "79",
        "Osmaniye" to "80",
        "Düzce" to "81"
    )

    return plates[city.name.trim()] ?: city.countryCode
}

/**
 * Şehir rozetinin görünümü.
 */
@Composable
private fun CountryBadge(
    code: String,
    isTurkishCity: Boolean,
    modifier: Modifier = Modifier
) {
    val badgeColor = if (isTurkishCity) {
        when ((code.toIntOrNull() ?: 0) % 3) {
            0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier.size(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = badgeColor.first,
        contentColor = badgeColor.second
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = code,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CityCardPreview() {
    HavaTheme {
        CityCard(
            city = City(
                id = 323786,
                name = "Ankara",
                latitude = 39.91987,
                longitude = 32.85427,
                countryCode = "TR",
                country = "Türkiye",
                admin1 = "Ankara"
            ),
            onClick = {}
        )
    }
}
