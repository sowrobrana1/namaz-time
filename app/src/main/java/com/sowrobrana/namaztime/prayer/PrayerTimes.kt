package com.sowrobrana.namaztime.prayer

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Offline prayer-time calculation (standard solar-astronomy method).
 * Angles: Fajr 18°, Isha 18° (matches published Bangladesh timetables
 * within ~1 minute — verified against Dhaka 2026-10-25: 04:44 / 06:00 /
 * 11:43 / 14:59 / 17:25 / 18:41). Asr uses the Shafi'i shadow factor (1).
 * Bangladesh has no daylight saving; tz is fixed at UTC+6.
 */
object PrayerTimes {

    /** All values are minutes from midnight, local time. */
    data class DayTimes(
        val fajr: Int,
        val sunrise: Int,
        val dhuhr: Int,
        val asr: Int,
        val maghrib: Int,
        val isha: Int,
    )

    private fun fixAngle(a: Double) = a - 360.0 * floor(a / 360.0)
    private fun fixHour(h: Double) = h - 24.0 * floor(h / 24.0)

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /** Returns declination (degrees) and equation of time (hours). */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(
            atan2(
                cos(Math.toRadians(e)) * sin(Math.toRadians(l)),
                cos(Math.toRadians(l)),
            ),
        ) / 15.0
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eq = q / 15.0 - fixHour(ra)
        return decl to eq
    }

    private fun angleTime(
        altDeg: Double,
        lat: Double,
        decl: Double,
        noon: Double,
        morning: Boolean,
    ): Double {
        val latR = Math.toRadians(lat)
        val declR = Math.toRadians(decl)
        var cosH = (sin(Math.toRadians(altDeg)) - sin(declR) * sin(latR)) /
            (cos(declR) * cos(latR))
        cosH = cosH.coerceIn(-1.0, 1.0)
        val h = Math.toDegrees(acos(cosH)) / 15.0
        return if (morning) noon - h else noon + h
    }

    fun compute(
        year: Int,
        month: Int,
        day: Int,
        lat: Double,
        lng: Double,
        tzHours: Double = 6.0,
    ): DayTimes {
        val jd = julianDay(year, month, day) - lng / 360.0
        val (decl, eq) = sunPosition(jd)
        val noon = fixHour(12 - eq)
        fun adj(t: Double) = fixHour(t + tzHours - lng / 15.0)
        fun toMin(t: Double): Int {
            val f = fixHour(t)
            var h = floor(f).toInt()
            var m = ((f - h) * 60).roundToInt()
            if (m == 60) {
                h += 1
                m = 0
            }
            return (h % 24) * 60 + m
        }
        val fajr = toMin(adj(angleTime(-18.0, lat, decl, noon, true)))
        val sunrise = toMin(adj(angleTime(-0.833, lat, decl, noon, true)))
        val dhuhr = toMin(adj(noon))
        val asrAlt = Math.toDegrees(
            atan(1.0 / (1.0 + tan(abs(Math.toRadians(lat) - Math.toRadians(decl))))),
        )
        val asr = toMin(adj(angleTime(asrAlt, lat, decl, noon, false)))
        val maghrib = toMin(adj(angleTime(-0.833, lat, decl, noon, false)))
        val isha = toMin(adj(angleTime(-18.0, lat, decl, noon, false)))
        return DayTimes(fajr, sunrise, dhuhr, asr, maghrib, isha)
    }
}
