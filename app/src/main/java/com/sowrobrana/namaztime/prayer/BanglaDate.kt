package com.sowrobrana.namaztime.prayer

import java.util.Calendar

/**
 * Bangladesh official (revised 2019) Bangla calendar.
 * Pohela Boishakh is always April 14; the first five months have 31
 * days, the rest 30 — except Falgun, which has 31 days when the
 * Gregorian year it ends in is a leap year.
 */
object BanglaDate {

    data class BanglaDate(val year: Int, val month: Int, val day: Int)

    val MONTH_NAMES_BN = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র",
    )

    private val MONTH_LENGTHS = listOf(31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 30, 30)

    fun fromGregorian(year: Int, month: Int, day: Int): BanglaDate {
        // Start of the Bangla year containing this date.
        val startYear = if (month > 4 || (month == 4 && day >= 14)) year else year - 1
        val banglaYear = startYear - 593
        // Falgun spans Feb–Mar of startYear + 1; leap iff that year is leap.
        val lengths = MONTH_LENGTHS.toMutableList()
        if (isLeap(startYear + 1)) lengths[10] = 31
        var remaining = daysBetween(startYear, 4, 14, year, month, day)
        var m = 0
        while (remaining >= lengths[m]) {
            remaining -= lengths[m]
            m++
        }
        return BanglaDate(banglaYear, m + 1, remaining + 1)
    }

    private fun isLeap(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

    private fun epochDay(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis / 86_400_000
    }

    private fun daysBetween(y1: Int, m1: Int, d1: Int, y2: Int, m2: Int, d2: Int): Int =
        (epochDay(y2, m2, d2) - epochDay(y1, m1, d1)).toInt()
}
