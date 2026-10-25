package com.sowrobrana.namaztime.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sowrobrana.namaztime.alarms.PrayerAlarmScheduler
import com.sowrobrana.namaztime.data.Prefs
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.City
import com.sowrobrana.namaztime.prayer.Hijri
import com.sowrobrana.namaztime.prayer.PrayerTimes
import com.sowrobrana.namaztime.util.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class PrayerRow(
    val name: String,
    val minutes: Int,
    val isSunrise: Boolean = false,
    /** End of this prayer's valid window in minutes, or -1 when it has none. */
    val windowEnd: Int = -1,
)

class NamazViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val context = app.applicationContext

    private val _cityIndex = MutableStateFlow(prefs.cityIndex)
    val cityIndex: StateFlow<Int> = _cityIndex.asStateFlow()

    private val _locationMode = MutableStateFlow(prefs.locationMode)
    val locationMode: StateFlow<String> = _locationMode.asStateFlow()

    private val _autoLabel = MutableStateFlow(prefs.autoLabel)
    val autoLabel: StateFlow<String> = _autoLabel.asStateFlow()

    private val _effectiveCity = MutableStateFlow(effectiveCity())
    val effectiveCity: StateFlow<City> = _effectiveCity.asStateFlow()

    private val _mazhab = MutableStateFlow(prefs.mazhab)
    val mazhab: StateFlow<Int> = _mazhab.asStateFlow()

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
        val city = effectiveCity()
        val factor = PrayerTimes.factorForMazhab(prefs.mazhab)
        val cal = Calendar.getInstance()
        val t = PrayerTimes.compute(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            city.lat,
            city.lng,
            asrFactor = factor,
        )
        // Isha's window runs until tomorrow's Fajr.
        val tomorrowCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tmr = PrayerTimes.compute(
            tomorrowCal.get(Calendar.YEAR),
            tomorrowCal.get(Calendar.MONTH) + 1,
            tomorrowCal.get(Calendar.DAY_OF_MONTH),
            city.lat,
            city.lng,
            asrFactor = factor,
        )
        return listOf(
            PrayerRow("ফজর", t.fajr, windowEnd = t.sunrise),
            PrayerRow("সূর্যোদয়", t.sunrise, isSunrise = true),
            PrayerRow("যোহর", t.dhuhr, windowEnd = t.asr),
            PrayerRow("আসর", t.asr, windowEnd = t.maghrib),
            PrayerRow("মাগরিব", t.maghrib, windowEnd = t.isha),
            PrayerRow("এশা", t.isha, windowEnd = tmr.fajr),
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

    /** The coordinates times are computed from: GPS position in auto mode, else picked district. */
    fun effectiveCity(): City {
        if (prefs.locationMode == "auto") {
            val lat = prefs.autoLat
            val lon = prefs.autoLon
            if (lat != null && lon != null) {
                val label = prefs.autoLabel.ifEmpty { LocationHelper.nearestCity(lat, lon).name }
                return City(label, lat, lon)
            }
        }
        return BD_CITIES.getOrElse(prefs.cityIndex) { BD_CITIES[0] }
    }

    /** Recompute rows + effective city + alarms after any location change. */
    private fun refreshAll() {
        _effectiveCity.value = effectiveCity()
        _today.value = todayRows()
        if (prefs.notifyEnabled) {
            PrayerAlarmScheduler.scheduleAll(
                context, effectiveCity(), PrayerTimes.factorForMazhab(prefs.mazhab),
            )
        }
    }

    fun setCity(index: Int) {
        prefs.cityIndex = index
        prefs.locationMode = "manual"
        _cityIndex.value = index
        _locationMode.value = "manual"
        refreshAll()
    }

    /** Switch between "auto" (GPS) and "manual" (picked district). */
    fun setLocationMode(mode: String) {
        prefs.locationMode = mode
        _locationMode.value = mode
        refreshAll()
    }

    /** Called with a fresh GPS fix: saves it, labels it with the nearest district, follows it. */
    fun setAutoLocation(lat: Double, lon: Double) {
        val nearest = LocationHelper.nearestCity(lat, lon)
        prefs.autoLat = lat
        prefs.autoLon = lon
        prefs.autoLabel = nearest.name
        prefs.locationMode = "auto"
        _autoLabel.value = nearest.name
        _locationMode.value = "auto"
        refreshAll()
    }

    fun setMazhab(mazhab: Int) {
        prefs.mazhab = mazhab
        _mazhab.value = mazhab
        _today.value = todayRows()
        if (prefs.notifyEnabled) {
            PrayerAlarmScheduler.scheduleAll(
                context, effectiveCity(), PrayerTimes.factorForMazhab(mazhab),
            )
        }
    }

    fun setNotify(enabled: Boolean) {
        prefs.notifyEnabled = enabled
        _notifyEnabled.value = enabled
        if (enabled) {
            PrayerAlarmScheduler.scheduleAll(
                context, effectiveCity(), PrayerTimes.factorForMazhab(prefs.mazhab),
            )
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

    /** Add n counts at once (e.g. counted on fingers or a physical tasbih). */
    fun addCounts(n: Int) {
        if (n <= 0) return
        val next = _tasbihCount.value + n
        prefs.setTasbihCount(_tasbihDhikr.value, next)
        _tasbihCount.value = next
    }

    fun resetTasbih() {
        prefs.setTasbihCount(_tasbihDhikr.value, 0)
        _tasbihCount.value = 0
    }
}
