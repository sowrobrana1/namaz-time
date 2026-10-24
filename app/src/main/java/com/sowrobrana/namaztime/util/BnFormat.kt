package com.sowrobrana.namaztime.util

private val BN_DIGITS = mapOf(
    '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
    '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯',
)

fun bnDigits(text: String): String = text.map { BN_DIGITS[it] ?: it }.joinToString("")

fun bnNumber(n: Int): String = bnDigits(n.toString())

/** "04:44" style 24h minutes → Bangla 12h string with period label, e.g. "৪:৪৪ ভোর". */
fun formatTimeBn(minutes: Int): String {
    val h24 = minutes / 60
    val m = minutes % 60
    val period = when (h24) {
        in 4..5 -> "ভোর"
        in 6..11 -> "সকাল"
        in 12..14 -> "দুপুর"
        in 15..16 -> "বিকাল"
        in 17..18 -> "সন্ধ্যা"
        else -> "রাত"
    }
    val h12 = when (h24 % 12) {
        0 -> 12
        else -> h24 % 12
    }
    return "${bnNumber(h12)}:${bnDigits(m.toString().padStart(2, '0'))} $period"
}

val BN_WEEKDAYS = listOf(
    "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার",
)

val BN_MONTHS = listOf(
    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর",
)
