package com.sowrobrana.namaztime.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sowrobrana.namaztime.alarms.PrayerAlarmScheduler
import com.sowrobrana.namaztime.data.Prefs
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.Hijri
import com.sowrobrana.namaztime.prayer.PrayerTimes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class PrayerRow(val name: String, val minutes: Int, val isSunrise: Boolean = false)

class NamazViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val context = app.applicationContext

    private val _cityIndex = MutableStateFlow(prefs.cityIndex)
    val cityIndex: StateFlow<Int> = _cityIndex.asStateFlow()

    private val _notifyEnabled = MutableStateFlow(prefs.notifyEnabled)
    val notifyEnabled: StateFlow<Boolean> = _notifyEnabled.asStateFlow()

    private val _today = MutableStateFlow(todayRows())
    val todayRows: StateFlow<List<PrayerRow>> = _today.asStateFlow()

    private val _hijri = MutableStateFlow(todayHijri())
    val hijri: StateFlow<Hijri.HijriDate> = _hijri.asStateFlow()

    /** Minutes from midnight, ticks every second so the countdown stays live. */
    private val _nowMinutes = MutableStateFlow(nowMinutes())
    val nowMinutes: StateFlow<Int> = _nowMinutes.asStateFlow()

    private val _tasbihDhikr = MutableStateFlow(prefs.tasbihDhikr)
    val tasbihDhikr: StateFlow<Int> = _tasbihDhikr.asStateFlow()

    private val _tasbihTarget = MutableStateFlow(prefs.tasbihTarget)
    val tasbihTarget: StateFlow<Int> = _tasbihTarget.asStateFlow()

    private val _tasbihCount = MutableStateFlow(prefs.tasbihCount(prefs.tasbihDhikr))
    val tasbihCount: StateFlow<Int> = _tasbihCount.asStateFlow()

    init {
        viewModelScope.launch {
            var lastDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            while (true) {
                delay(1000)
                _nowMinutes.value = nowMinutes()
                val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                if (day != lastDay) {
                    lastDay = day
                    _today.value = todayRows()
                    _hijri.value = todayHijri()
                }
            }
        }
    }

    private fun nowMinutes(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    private fun todayRows(): List<PrayerRow> {
        val city = BD_CITIES.getOrElse(prefs.cityIndex) { BD_CITIES[0] }
        val cal = Calendar.getInstance()
        val t = PrayerTimes.compute(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            city.lat,
            city.lng,
        )
        return listOf(
            PrayerRow("ফজর", t.fajr),
            PrayerRow("সূর্যোদয়", t.sunrise, isSunrise = true),
            PrayerRow("যোহর", t.dhuhr),
            PrayerRow("আসর", t.asr),
            PrayerRow("মাগরিব", t.maghrib),
            PrayerRow("এশা", t.isha),
        )
    }

    private fun todayHijri(): Hijri.HijriDate {
        val cal = Calendar.getInstance()
        return Hijri.fromGregorian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )
    }

    fun setCity(index: Int) {
        prefs.cityIndex = index
        _cityIndex.value = index
        _today.value = todayRows()
        if (prefs.notifyEnabled) {
            PrayerAlarmScheduler.scheduleAll(context, BD_CITIES[index])
        }
    }

    fun setNotify(enabled: Boolean) {
        prefs.notifyEnabled = enabled
        _notifyEnabled.value = enabled
        val city = BD_CITIES.getOrElse(prefs.cityIndex) { BD_CITIES[0] }
        if (enabled) {
            PrayerAlarmScheduler.scheduleAll(context, city)
        } else {
            PrayerAlarmScheduler.cancelAll(context)
        }
    }

    fun selectDhikr(index: Int) {
        prefs.tasbihDhikr = index
        _tasbihDhikr.value = index
        _tasbihCount.value = prefs.tasbihCount(index)
    }

    fun setTarget(target: Int) {
        prefs.tasbihTarget = target
        _tasbihTarget.value = target
    }

    fun tapTasbih() {
        val next = _tasbihCount.value + 1
        prefs.setTasbihCount(_tasbihDhikr.value, next)
        _tasbihCount.value = next
    }

    fun resetTasbih() {
        prefs.setTasbihCount(_tasbihDhikr.value, 0)
        _tasbihCount.value = 0
    }
}
