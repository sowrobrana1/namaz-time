package com.sowrobrana.namaztime.data

import android.content.Context
import android.content.SharedPreferences

/** Tiny key-value store: city, notification toggle, tasbih counters. */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("namaz_prefs", Context.MODE_PRIVATE)

    var cityIndex: Int
        get() = sp.getInt("city_index", 0)
        set(value) = sp.edit().putInt("city_index", value).apply()

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

    fun tasbihCount(dhikr: Int): Int = sp.getInt("tasbih_count_$dhikr", 0)

    fun setTasbihCount(dhikr: Int, count: Int) {
        sp.edit().putInt("tasbih_count_$dhikr", count).apply()
    }
}
