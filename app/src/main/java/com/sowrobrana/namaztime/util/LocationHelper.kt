package com.sowrobrana.namaztime.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.City
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * GPS location for the auto-location mode (Muslims Day style):
 * permission -> current position -> nearest district -> times follow it.
 * Fully offline: no Play Services, no network geocoder.
 */
object LocationHelper {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    /** Nearest district-town to the given coordinates. */
    fun nearestCity(lat: Double, lon: Double): City {
        return BD_CITIES.minByOrNull { haversineKm(lat, lon, it.lat, it.lng) } ?: BD_CITIES[0]
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
            sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    /**
     * Best-effort current position. Last-known GPS/Network fix first (instant);
     * otherwise one fresh fix (up to ~10s); otherwise [onEmpty].
     * Call only after [hasPermission] is true.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrent(
        context: Context,
        onResult: (Double, Double) -> Unit,
        onEmpty: () -> Unit,
    ) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { lm.isProviderEnabled(it) }
        var best: Location? = null
        for (p in providers) {
            val loc = try {
                lm.getLastKnownLocation(p)
            } catch (_: SecurityException) {
                null
            } catch (_: Exception) {
                null
            }
            if (loc != null && (best == null || loc.accuracy < best.accuracy)) best = loc
        }
        if (best != null) {
            onResult(best.latitude, best.longitude)
            return
        }
        if (providers.isEmpty()) {
            onEmpty()
            return
        }
        var done = false
        val handler = Handler(Looper.getMainLooper())
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                if (done) return
                done = true
                try {
                    lm.removeUpdates(this)
                } catch (_: Exception) {
                }
                onResult(loc.latitude, loc.longitude)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        try {
            for (p in providers) {
                lm.requestLocationUpdates(p, 0L, 0f, listener, Looper.getMainLooper())
            }
        } catch (_: Exception) {
            onEmpty()
            return
        }
        handler.postDelayed({
            if (done) return@postDelayed
            done = true
            try {
                lm.removeUpdates(listener)
            } catch (_: Exception) {
            }
            onEmpty()
        }, 10_000)
    }
}
