package com.sowrobrana.namaztime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sowrobrana.namaztime.prayer.BanglaDate
import com.sowrobrana.namaztime.prayer.Hijri
import com.sowrobrana.namaztime.prayer.Holidays
import com.sowrobrana.namaztime.util.BN_MONTHS
import com.sowrobrana.namaztime.util.bnNumber
import java.util.Calendar

private val WEEKDAYS_SHORT = listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র")
private val EMERALD = Color(0xFF0E7A5B)
private val GOLD = Color(0xFFB8860B)

/**
 * Triple calendar: one month grid where every day shows its
 * Gregorian, Bangla and Hijri dates together, with government
 * holidays and Islamic observances marked.
 */
@Composable
fun CalendarScreen() {
    val today = remember { Calendar.getInstance() }
    var viewYear by remember { mutableStateOf(today.get(Calendar.YEAR)) }
    var viewMonth by remember { mutableStateOf(today.get(Calendar.MONTH) + 1) }
    var selectedDay by remember {
        mutableStateOf(
            if (viewYear == today.get(Calendar.YEAR) &&
                viewMonth == today.get(Calendar.MONTH) + 1
            ) {
                today.get(Calendar.DAY_OF_MONTH)
            } else {
                1
            },
        )
    }

    fun shiftMonth(delta: Int) {
        var y = viewYear
        var m = viewMonth + delta
        while (m < 1) {
            m += 12
            y -= 1
        }
        while (m > 12) {
            m -= 12
            y += 1
        }
        viewYear = y
        viewMonth = m
        val isCurrentMonth =
            y == today.get(Calendar.YEAR) && m == today.get(Calendar.MONTH) + 1
        selectedDay = if (isCurrentMonth) today.get(Calendar.DAY_OF_MONTH) else 1
    }

    val firstCal = Calendar.getInstance().apply {
        set(viewYear, viewMonth - 1, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val daysInMonth = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Saturday-first offset: Calendar.SATURDAY = 7 -> 0, SUNDAY = 1 -> 1 ...
    val offset = firstCal.get(Calendar.DAY_OF_WEEK) % 7
    val lastDay = daysInMonth

    // Month names spanned by this Gregorian month, for the sub-header.
    val banglaFirst = BanglaDate.fromGregorian(viewYear, viewMonth, 1)
    val banglaLast = BanglaDate.fromGregorian(viewYear, viewMonth, lastDay)
    val banglaSpan = if (banglaFirst.month == banglaLast.month) {
        "${BanglaDate.MONTH_NAMES_BN[banglaFirst.month - 1]} ${bnNumber(banglaFirst.year)}"
    } else {
        "${BanglaDate.MONTH_NAMES_BN[banglaFirst.month - 1]}–" +
            "${BanglaDate.MONTH_NAMES_BN[banglaLast.month - 1]} ${bnNumber(banglaLast.year)}"
    }
    val hijriFirst = Hijri.fromGregorian(viewYear, viewMonth, 1)
    val hijriLast = Hijri.fromGregorian(viewYear, viewMonth, lastDay)
    val hijriSpan = if (hijriFirst.month == hijriLast.month) {
        "${Hijri.MONTH_NAMES_BN[hijriFirst.month - 1]} ${bnNumber(hijriFirst.year)}"
    } else {
        "${Hijri.MONTH_NAMES_BN[hijriFirst.month - 1]}–" +
            "${Hijri.MONTH_NAMES_BN[hijriLast.month - 1]} ${bnNumber(hijriLast.year)}"
    }

    val selBangla = BanglaDate.fromGregorian(viewYear, viewMonth, selectedDay)
    val selHijri = Hijri.fromGregorian(viewYear, viewMonth, selectedDay)
    val selEvents = Holidays.eventsOn(viewYear, viewMonth, selectedDay)
    val isToday = viewYear == today.get(Calendar.YEAR) &&
        viewMonth == today.get(Calendar.MONTH) + 1 &&
        selectedDay == today.get(Calendar.DAY_OF_MONTH)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Month navigation.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { shiftMonth(-1) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "আগের মাস")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${BN_MONTHS[viewMonth - 1]} ${bnNumber(viewYear)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$banglaSpan • $hijriSpan",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
            IconButton(onClick = { shiftMonth(1) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "পরের মাস")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = {
                viewYear = today.get(Calendar.YEAR)
                viewMonth = today.get(Calendar.MONTH) + 1
                selectedDay = today.get(Calendar.DAY_OF_MONTH)
            }) {
                Text("আজ", fontSize = 13.sp)
            }
        }

        // Weekday header.
        Row(modifier = Modifier.fillMaxWidth()) {
            WEEKDAYS_SHORT.forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Day grid (Saturday-first).
        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val day = cellIndex - offset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .border(
                                width = if (day == selectedDay) 2.dp else 0.dp,
                                color = if (day == selectedDay) EMERALD else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .background(
                                color = if (day == selectedDay) Color(0xFFE8F5E9) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .clickable(enabled = day in 1..daysInMonth) {
                                selectedDay = day
                            }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (day in 1..daysInMonth) {
                            val b = BanglaDate.fromGregorian(viewYear, viewMonth, day)
                            val h = Hijri.fromGregorian(viewYear, viewMonth, day)
                            val events = Holidays.eventsOn(viewYear, viewMonth, day)
                            val isCellToday = isToday && day == selectedDay ||
                                (viewYear == today.get(Calendar.YEAR) &&
                                    viewMonth == today.get(Calendar.MONTH) + 1 &&
                                    day == today.get(Calendar.DAY_OF_MONTH))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = bnNumber(day),
                                    fontSize = 15.sp,
                                    fontWeight = if (isCellToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (events.isNotEmpty()) Color(0xFFC62828) else Color.Black,
                                )
                                Text(
                                    text = bnNumber(b.day),
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                )
                                Text(
                                    text = bnNumber(h.day),
                                    fontSize = 10.sp,
                                    color = Color(0xFF2E7D32),
                                )
                                if (events.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(Color(0xFFC62828), shape = CircleShape),
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "প্রতিটি ঘরে: ইংরেজি তারিখ (বড়), বাংলা তারিখ (ধূসর), আরবি তারিখ (সবুজ)। লাল দাগ = ছুটি বা দিবস।",
            fontSize = 12.sp,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Selected-day detail.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${bnNumber(selectedDay)} ${BN_MONTHS[viewMonth - 1]} ${bnNumber(viewYear)}" +
                        if (isToday) " (আজ)" else "",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "বাংলা: ${bnNumber(selBangla.day)} " +
                        "${BanglaDate.MONTH_NAMES_BN[selBangla.month - 1]} ${bnNumber(selBangla.year)}",
                    fontSize = 15.sp,
                )
                Text(
                    text = "আরবি: ${bnNumber(selHijri.day)} " +
                        "${Hijri.MONTH_NAMES_BN[selHijri.month - 1]} ${bnNumber(selHijri.year)}",
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (selEvents.isEmpty()) {
                    Text(
                        text = "এই দিনে কোনো সরকারি ছুটি বা দিবস নেই।",
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                } else {
                    selEvents.forEach { e ->
                        Text(
                            text = "• $e",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC62828),
                        )
                    }
                }
                if (Holidays.isRamadan(viewYear, viewMonth, selectedDay)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "রমজান মাস — সেহরির শেষ সময় ফজরের ওয়াক্তে, ইফতার মাগরিবের ওয়াক্তে।",
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "হিজরি তারিখ হিসাবনির্ভর — চাঁদ দেখা সাপেক্ষে ১ দিন এদিক-ওদিক হতে পারে।",
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}
