package com.kampplus.hava.ui.city

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Approximate device location, using Android's built-in location service. */
class CurrentLocationProvider(context: Context) {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission") // Caller checks and requests COARSE_LOCATION.
    suspend fun getLocation(): Location = suspendCancellableCoroutine { continuation ->
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }
        if (providers.isEmpty()) {
            continuation.resumeWith(Result.failure(IllegalStateException("Konum servisleri kapalı.")))
            return@suspendCancellableCoroutine
        }

        var bestLocation: Location? = null
        var listener: LocationListener? = null
        val handler = Handler(Looper.getMainLooper())
        fun finish(location: Location?) {
            listener?.let(manager::removeUpdates)
            handler.removeCallbacksAndMessages(null)
            if (continuation.isActive) {
                if (location != null) continuation.resume(location)
                else continuation.resumeWith(Result.failure(IllegalStateException("Konum alınamadı. Tekrar deneyin.")))
            }
        }

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (bestLocation == null || location.accuracy < bestLocation!!.accuracy) bestLocation = location
                // Network location is quick; briefly allow GPS to provide a better fix.
                if (location.provider == LocationManager.GPS_PROVIDER) finish(location)
                else handler.postDelayed({ finish(bestLocation) }, 1_500)
            }
            @Deprecated("Deprecated by Android") override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }

        continuation.invokeOnCancellation { listener?.let(manager::removeUpdates); handler.removeCallbacksAndMessages(null) }
        providers.forEach { provider ->
            runCatching { manager.requestLocationUpdates(provider, 0L, 0f, listener!!, Looper.getMainLooper()) }
        }
        handler.postDelayed({ finish(bestLocation) }, 15_000)
    }
}
