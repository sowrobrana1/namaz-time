package com.sowrobrana.namaztime.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sowrobrana.namaztime.data.Prefs
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.Hijri
import com.sowrobrana.namaztime.prayer.PrayerTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val index = intent.getIntExtra("prayer_index", -1)
                if (index in 0 until 5) {
                    // Ramadan: the Fajr alarm is the sehri-end alarm and
                    // the Maghrib alarm is the iftar alarm — label them so.
                    val name = PrayerAlarmScheduler.PRAYER_NAMES[index]
                    val nowCal = java.util.Calendar.getInstance()
                    val isRamadan = Hijri.fromGregorian(
                        nowCal.get(java.util.Calendar.YEAR),
                        nowCal.get(java.util.Calendar.MONTH) + 1,
                        nowCal.get(java.util.Calendar.DAY_OF_MONTH),
                    ).month == 9
                    val (title, subtitle) = when {
                        isRamadan && index == 0 ->
                            "সেহরির শেষ সময়" to "ফজরের আজান — এখন সেহরির সময় শেষ"
                        isRamadan && index == 3 ->
                            "ইফতারের সময়" to "মাগরিবের আজান — এখন ইফতারের সময়"
                        else -> "$name-এর আজান" to "এখন $name-এর ওয়াক্ত শুরু হয়েছে"
                    }
                    NotificationHelper.showAzanAlarm(
                        context,
                        title,
                        subtitle,
                        2000 + index,
                    )
                }
                // Re-chain: keep tomorrow's alarms scheduled after every fire.
                val prefs = Prefs(context)
                if (prefs.notifyEnabled) {
                    val city = BD_CITIES.getOrElse(prefs.cityIndex) { BD_CITIES[0] }
                    PrayerAlarmScheduler.scheduleAll(
                        context, city, PrayerTimes.factorForMazhab(prefs.mazhab),
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val prefs = Prefs(context)
                if (prefs.notifyEnabled) {
                    val city = BD_CITIES.getOrElse(prefs.cityIndex) { BD_CITIES[0] }
                    PrayerAlarmScheduler.scheduleAll(
                        context, city, PrayerTimes.factorForMazhab(prefs.mazhab),
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}
