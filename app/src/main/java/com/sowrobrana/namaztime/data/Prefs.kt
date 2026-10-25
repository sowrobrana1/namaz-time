package com.sowrobrana.namaztime.data

import android.content.Context
import android.content.SharedPreferences
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.City
import com.sowrobrana.namaztime.util.LocationHelper

/** Tiny key-value store: city, notification toggle, tasbih counters. */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("namaz_prefs", Context.MODE_PRIVATE)

    var cityIndex: Int
        get() = sp.getInt("city_index", 0)
        set(value) = sp.edit().putInt("city_index", value).apply()

    /** "manual" = picked district, "auto" = GPS position (nearest district label). */
    var locationMode: String
        get() = sp.getString("location_mode", "manual") ?: "manual"
        set(value) = sp.edit().putString("location_mode", value).apply()

    /** Last GPS position used in auto mode; null when never located. */
    var autoLat: Double?
        get() = sp.getString("auto_lat", null)?.toDoubleOrNull()
        set(value) = sp.edit().putString("auto_lat", value?.toString()).apply()

    var autoLon: Double?
        get() = sp.getString("auto_lon", null)?.toDoubleOrNull()
        set(value) = sp.edit().putString("auto_lon", value?.toString()).apply()

    /** Nearest-district label for the last GPS position. */
    var autoLabel: String
        get() = sp.getString("auto_label", "") ?: ""
        set(value) = sp.edit().putString("auto_label", value).apply()

    /** 0 = Hanafi (default), 1 = Shafi'i. Only affects Asr time. */
    var mazhab: Int
        get() = sp.getInt("mazhab", 0)
        set(value) = sp.edit().putInt("mazhab", value).apply()

    var notifyEnabled: Boolean
        get() = sp.getBoolean("notify_enabled", true)
        set(value) = sp.edit().putBoolean("notify_enabled", value).apply()

    var tasbihDhikr: Int
        get() = sp.getInt("tasbih_dhikr", 0)
        set(value) = sp.edit().putInt("tasbih_dhikr", value).apply()

    var tasbihTarget: Int
        get() = sp.getInt("tasbih_target", 33)
        set(value) = sp.edit().putInt("tasbih_target", value).apply()

    /** Last Quran position for the "continue reading" card (0 = none). */
    var lastQuranSurah: Int
        get() = sp.getInt("last_quran_surah", 0)
        set(value) = sp.edit().putInt("last_quran_surah", value).apply()

    var lastQuranAyah: Int
        get() = sp.getInt("last_quran_ayah", 1)
        set(value) = sp.edit().putInt("last_quran_ayah", value).apply()

    fun tasbihCount(dhikr: Int): Int = sp.getInt("tasbih_count_$dhikr", 0)

    /** The city prayer times/alarms must follow: GPS position in auto
     * mode (nearest district label), otherwise the picked district.
     * Used by the ViewModel AND by the alarm receivers, so scheduled
     * alarms never drift back to the manual district in auto mode. */
    fun effectiveCity(): City {
        if (locationMode == "auto") {
            val lat = autoLat
            val lon = autoLon
            if (lat != null && lon != null) {
                val nearest = LocationHelper.nearestCity(lat, lon)
                return City(autoLabel.ifEmpty { nearest.name }, lat, lon)
            }
        }
        return BD_CITIES.getOrElse(cityIndex) { BD_CITIES[0] }
    }

    fun setTasbihCount(dhikr: Int, count: Int) {
        sp.edit().putInt("tasbih_count_$dhikr", count).apply()
    }
}
