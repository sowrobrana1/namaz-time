package com.sowrobrana.namaztime.prayer

/**
 * Holidays and observances shown on the calendar.
 *
 * Fixed Gregorian dates repeat every year. Islamic observances are
 * keyed to Hijri dates and computed with [Hijri], so they move with
 * the lunar calendar. A few dates that depend on the Hindu panjika
 * or a yearly gazette (Durga Puja, Janmashtami, Eid surrounding
 * holidays) are hard-coded for 2026 only, from the government's
 * official 2026 holiday announcement (Daily Star, Nov 2025).
 * Moon-sighting can shift Islamic dates by a day.
 */
object Holidays {

    /** Fixed (month, day) -> name, repeats every year. */
    private val FIXED = mapOf(
        Pair(2, 21) to "শহীদ দিবস ও আন্তর্জাতিক মাতৃভাষা দিবস",
        Pair(3, 26) to "স্বাধীনতা দিবস",
        Pair(4, 14) to "পহেলা বৈশাখ",
        Pair(5, 1) to "মহান মে দিবস ও বুদ্ধ পূর্ণিমা",
        Pair(8, 5) to "জুলাই গণঅভ্যুত্থান দিবস",
        Pair(12, 16) to "বিজয় দিবস",
        Pair(12, 25) to "বড়দিন",
    )

    /** Hijri (month, day) -> name, computed every year. */
    private val HIJRI_EVENTS = mapOf(
        Pair(1, 10) to "আশুরা",
        Pair(3, 12) to "ঈদে মিলাদুন্নবী",
        Pair(7, 27) to "শবে মেরাজ",
        Pair(8, 15) to "শবে বরাত",
        Pair(9, 1) to "রমজান শুরু",
        Pair(9, 27) to "শবে কদর",
        Pair(10, 1) to "ঈদুল ফিতর",
        Pair(12, 10) to "ঈদুল আজহা",
    )

    /** 2026-only dates (year, month, day) -> name. */
    private val YEAR_2026 = mapOf(
        Triple(2026, 3, 19) to "ঈদুল ফিতরের ছুটি",
        Triple(2026, 3, 20) to "জুমাতুল বিদা",
        Triple(2026, 3, 22) to "ঈদুল ফিতরের ছুটি",
        Triple(2026, 3, 23) to "ঈদুল ফিতরের ছুটি",
        Triple(2026, 5, 26) to "ঈদুল আজহার ছুটি",
        Triple(2026, 5, 27) to "ঈদুল আজহার ছুটি",
        Triple(2026, 5, 29) to "ঈদুল আজহার ছুটি",
        Triple(2026, 5, 30) to "ঈদুল আজহার ছুটি",
        Triple(2026, 5, 31) to "ঈদুল আজহার ছুটি",
        Triple(2026, 9, 4) to "জন্মাষ্টমী",
        Triple(2026, 10, 20) to "দুর্গাপূজা (মহানবমী)",
        Triple(2026, 10, 21) to "দুর্গাপূজা (বিজয়া দশমী)",
    )

    /** All holiday/observance names for one Gregorian date. */
    fun eventsOn(year: Int, month: Int, day: Int): List<String> {
        val out = mutableListOf<String>()
        FIXED[Pair(month, day)]?.let { out += it }
        val hijri = Hijri.fromGregorian(year, month, day)
        HIJRI_EVENTS[Pair(hijri.month, hijri.day)]?.let { out += it }
        YEAR_2026[Triple(year, month, day)]?.let { out += it }
        return out
    }

    fun isRamadan(year: Int, month: Int, day: Int): Boolean =
        Hijri.fromGregorian(year, month, day).month == 9
}
