package com.sowrobrana.namaztime.prayer

import kotlin.math.floor

/**
 * Approximate Hijri date via the Kuwaiti algorithm.
 * Validated: 2026-10-25 → 14 Jumada al-Awwal 1448, matching published
 * Bangladesh Hijri calendars. A ±1 day difference vs the nationally
 * sighted moon is possible, as with every calculated calendar.
 */
object Hijri {

    data class HijriDate(val year: Int, val month: Int, val day: Int)

    val MONTH_NAMES_BN = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ",
    )

    fun fromGregorian(year: Int, month: Int, day: Int): HijriDate {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
        var l = floor(jd + 0.5).toLong() + 1 - 1948440 + 10632
        val n = (l - 1) / 10631
        l = l - 10631 * n + 354
        val j = ((10985 - l) / 5316) * ((50 * l) / 17719) + (l / 5670) * ((43 * l) / 15238)
        l = l - ((30 - j) / 15) * ((17719 * j) / 50) - ((j / 16) * ((15238 * j) / 43)) + 29
        val hMonth = (24 * l / 709).toInt()
        val hDay = (l - 709 * hMonth / 24).toInt()
        val hYear = (30 * n + j - 30).toInt()
        return HijriDate(hYear, hMonth, hDay)
    }
}
