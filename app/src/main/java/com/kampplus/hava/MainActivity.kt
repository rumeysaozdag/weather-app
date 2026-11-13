package com.kampplus.hava

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.kampplus.hava.data.FavoriteCityStore
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.HavaNavHost
import com.kampplus.hava.ui.LaunchSplashScreen
import com.kampplus.hava.ui.theme.HavaTheme
import kotlinx.coroutines.delay

/**
 * Uygulamanın tek Activity'si; kullanıcının girdiği kapı.
 *
 * Activity bir **pencereyi** yönetir. Pencerenin içine ne çizileceğini ise
 * Compose söyler: sahne Activity'dir, dekoru Compose kurar.
 */
class MainActivity : ComponentActivity() {

    /**
     * Ekran oluşturulurken Android bu fonksiyonu **kendisi** çağırır; biz çağırmayız.
     * Yaşam döngüsünün ilk adımıdır.
     *
     * Logcat'i açıp uygulamayı arka plana alıp geri dönerek `onCreate` / `onStart` /
     * `onResume` sırasını gözlemleyebilirsin.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        // İçeriği durum ve navigasyon çubuklarının altına kadar yayar; mor tema üzerinde beyaz sistem ikonları kullanır.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        // Süslü parantezin içi Compose dünyasıdır.
        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            var darkTheme by rememberSaveable { mutableStateOf(systemDarkTheme) }
            var showLaunchScreen by rememberSaveable { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(1400)
                showLaunchScreen = false
            }
            val favoriteStore = remember { FavoriteCityStore(applicationContext) }
            val favorites = remember {
                mutableStateListOf<City>().apply { addAll(favoriteStore.read()) }
            }
            HavaTheme(darkTheme = darkTheme) {
                if (showLaunchScreen) {
                    LaunchSplashScreen()
                } else {
                    HavaNavHost(
                        favoriteCities = favorites,
                        onToggleFavorite = { city ->
                            val index = favorites.indexOfFirst { it.id == city.id }
                            if (index >= 0) favorites.removeAt(index) else favorites.add(city)
                            favoriteStore.write(favorites.toList())
                        },
                        onToggleTheme = { darkTheme = !darkTheme }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    private companion object {
        /** Logcat'te filtrelemek için: arama kutusuna `Lifecycle` yaz. */
        const val TAG = "Lifecycle"
    }
}
