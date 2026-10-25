package com.sowrobrana.namaztime.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.City
import com.sowrobrana.namaztime.prayer.PrayerTimes
import java.util.Calendar

/** Exact daily alarms for the 5 prayers (sunrise excluded). */
object PrayerAlarmScheduler {

    val PRAYER_NAMES = listOf("ফজর", "যোহর", "আসর", "মাগরিব", "এশা")

    private fun timesFor(city: City, cal: Calendar, asrFactor: Double): List<Int> {
        val t = PrayerTimes.compute(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            city.lat,
            city.lng,
            asrFactor = asrFactor,
        )
        return listOf(t.fajr, t.dhuhr, t.asr, t.maghrib, t.isha)
    }

    /** Schedule today's remaining prayers + all of tomorrow's (so the chain never gaps). */
    fun scheduleAll(context: Context, city: City, asrFactor: Double = 1.0) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()
        val today = timesFor(city, now, asrFactor)
        val tomorrowCal = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrow = timesFor(city, tomorrowCal, asrFactor)
        for (i in 0 until 5) {
            scheduleOne(context, am, i, now, today[i])
            scheduleOne(context, am, 100 + i, tomorrowCal, tomorrow[i])
        }
    }

    private fun scheduleOne(
        context: Context,
        am: AlarmManager,
        code: Int,
        day: Calendar,
        minutes: Int,
    ) {
        val cal = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) return
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra("prayer_index", code % 100)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        // Never crash over an alarm: on Android 12+ exact alarms need the
        // SCHEDULE_EXACT_ALARM permission, which a fresh install does NOT
        // have until the user allows it in Settings. Without the guard
        // below, setExactAndAllowWhileIdle throws SecurityException on
        // every launch (notify defaults to on) — the "app has a bug"
        // crash loop. Fall back to an inexact alarm until granted.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !am.canScheduleExactAlarms()
            ) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            }
        } catch (_: SecurityException) {
            try {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } catch (_: SecurityException) {
                // Leave notifications off until the permission is granted.
            }
        }
    }

    fun cancelAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (code in 0 until 5) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            am.cancel(pi)
        }
        for (code in 100 until 105) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            am.cancel(pi)
        }
    }

    fun defaultCity(): City = BD_CITIES[0]
}
