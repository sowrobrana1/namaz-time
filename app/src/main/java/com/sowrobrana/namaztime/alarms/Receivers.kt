package com.sowrobrana.namaztime.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sowrobrana.namaztime.data.Prefs
import com.sowrobrana.namaztime.prayer.BD_CITIES
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
                    NotificationHelper.showPrayerNotification(
                        context,
                        PrayerAlarmScheduler.PRAYER_NAMES[index],
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
