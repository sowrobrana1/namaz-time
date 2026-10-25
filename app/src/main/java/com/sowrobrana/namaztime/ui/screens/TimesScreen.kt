package com.sowrobrana.namaztime.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.Hijri
import com.sowrobrana.namaztime.ui.NamazViewModel
import com.sowrobrana.namaztime.ui.PrayerRow
import com.sowrobrana.namaztime.util.BN_MONTHS
import com.sowrobrana.namaztime.util.BN_WEEKDAYS
import com.sowrobrana.namaztime.util.bnDigits
import com.sowrobrana.namaztime.util.bnNumber
import com.sowrobrana.namaztime.util.formatTimeBn
import java.util.Calendar

@Composable
fun TimesScreen(vm: NamazViewModel, onRequestExactAlarm: () -> Unit) {
    val cityIndex by vm.cityIndex.collectAsState()
    val mazhab by vm.mazhab.collectAsState()
    val notifyEnabled by vm.notifyEnabled.collectAsState()
    val rows by vm.todayRows.collectAsState()
    val hijri by vm.hijri.collectAsState()
    val now by vm.nowMinutes.collectAsState()
    val context = LocalContext.current
    val city = BD_CITIES.getOrElse(cityIndex) { BD_CITIES[0] }

    val prayers = rows.filter { !it.isSunrise }
    val (nextName, nextMin, prevMin) = nextPrayerInfo(prayers, now)
    val remaining = nextMin - now
    val total = (nextMin - prevMin).coerceAtLeast(1)
    val progress = (1f - remaining.toFloat() / total).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 120f),
        label = "countdownRing",
    )

    val cal = Calendar.getInstance()
    val gregorian = "${BN_WEEKDAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]}, " +
        "${bnNumber(cal.get(Calendar.DAY_OF_MONTH))} " +
        "${BN_MONTHS[cal.get(Calendar.MONTH)]} ${bnNumber(cal.get(Calendar.YEAR))}"
    val hijriText = "${bnNumber(hijri.day)} ${Hijri.MONTH_NAMES_BN[hijri.month - 1]} " +
        "${bnNumber(hijri.year)} হিজরি"

    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
    } else {
        true
    }

    var cityDialogOpen by remember { mutableStateOf(false) }
    var cityQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Header: emerald gradient, city picker, dates, countdown ring.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0B3D2E), Color(0xFF0E7A5B), Color(0xFF14906B)),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = { cityDialogOpen = true }) {
                    Text(
                        text = city.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
                Text(text = gregorian, color = Color(0xFFD9EDE4), fontSize = 14.sp)
                Text(text = hijriText, color = Color(0xFFE8B84B), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                // Countdown ring.
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 14.dp.toPx()
                        drawArc(
                            color = Color.White.copy(alpha = 0.25f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = Color(0xFFE8B84B),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "পরবর্তী নামাজ",
                            color = Color(0xFFD9EDE4),
                            fontSize = 13.sp,
                        )
                        Text(
                            text = nextName,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = formatRemaining(remaining),
                            color = Color(0xFFE8B84B),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "আজকের সময়সূচি • ${city.name}",
                    color = Color(0xFFD9EDE4),
                    fontSize = 12.sp,
                )
            }
        }

        // District picker: searchable dialog (64 districts).
        if (cityDialogOpen) {
            AlertDialog(
                onDismissRequest = { cityDialogOpen = false },
                title = { Text("জেলা বেছে নিন") },
                text = {
                    Column {
                        TextField(
                            value = cityQuery,
                            onValueChange = { cityQuery = it },
                            label = { Text("খুঁজুন") },
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val filtered = BD_CITIES.filter {
                            it.name.contains(cityQuery.trim())
                        }
                        LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                            items(filtered) { c ->
                                TextButton(
                                    onClick = {
                                        vm.setCity(BD_CITIES.indexOf(c))
                                        cityDialogOpen = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(c.name)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { cityDialogOpen = false }) { Text("বাতিল") }
                },
            )
        }

        if (notifyEnabled && !canScheduleExact) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 12.dp, 16.dp, 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF0D5)),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "সঠিক অ্যালার্মের অনুমতি দরকার",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "না দিলে সময়মতো নোটিফিকেশন আসতে দেরি হতে পারে।",
                        fontSize = 13.sp,
                    )
                    TextButton(onClick = onRequestExactAlarm) {
                        Text("অনুমতি দিন")
                    }
                }
            }
        }

        // Notification toggle.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 12.dp, 16.dp, 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (notifyEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "  ওয়াক্তের নোটিফিকেশন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Switch(checked = notifyEnabled, onCheckedChange = { vm.setNotify(it) })
        }

        // Mazhab selector (only Asr time differs between the two).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 4.dp, 16.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "মাজহাব",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mazhab == 0,
                    onClick = { vm.setMazhab(0) },
                    label = { Text("হানাফি", fontSize = 13.sp) },
                )
                FilterChip(
                    selected = mazhab == 1,
                    onClick = { vm.setMazhab(1) },
                    label = { Text("শাফেয়ি", fontSize = 13.sp) },
                )
            }
        }
        Text(
            text = "শুধু আসরের সময় মাজহাব অনুযায়ী বদলায় (হানাফি আসর দেরিতে)।",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 4.dp),
        )

        // Prayer rows, staggered entrance.
        val currentName = prayers.lastOrNull { it.minutes <= now }?.name
        rows.forEachIndexed { index, row ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { 40 + index * 12 },
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 450,
                        delayMillis = index * 70,
                        easing = FastOutSlowInEasing,
                    ),
                ) + fadeIn(),
            ) {
                PrayerRowCard(
                    row = row,
                    isCurrent = row.name == currentName,
                    isNext = row.name == nextName,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PrayerRowCard(row: PrayerRow, isCurrent: Boolean, isNext: Boolean) {
    val container = when {
        isCurrent -> Color(0xFFD9EDE4)
        isNext -> Color(0xFFF5E7C3)
        else -> Color.White
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent || isNext) 3.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF0E7A5B), shape = RoundedCornerShape(5.dp)),
                        )
                        Text(text = "  ", fontSize = 1.sp)
                    }
                    Text(
                        text = row.name,
                        fontSize = 17.sp,
                        fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.Medium,
                    )
                    if (row.isSunrise) {
                        Text(text = "  (নিষিদ্ধ সময় নয়)", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                if (row.windowEnd >= 0) {
                    Text(
                        text = "ওয়াক্ত: ${formatTimeBn(row.minutes)} – ${formatTimeBn(row.windowEnd)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isNext) {
                    Text(
                        text = "পরবর্তী  ",
                        fontSize = 12.sp,
                        color = Color(0xFF8A6A12),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = formatTimeBn(row.minutes),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Returns (nextName, nextMinutesAbsolute, prevMinutesAbsolute) in a continuous timeline. */
private fun nextPrayerInfo(prayers: List<PrayerRow>, now: Int): Triple<String, Int, Int> {
    val sorted = prayers.sortedBy { it.minutes }
    val next = sorted.firstOrNull { it.minutes > now }
    if (next != null) {
        val prev = sorted.lastOrNull { it.minutes <= now }?.minutes
            ?: (sorted.last().minutes - 1440)
        return Triple(next.name, next.minutes, prev)
    }
    val first = sorted.first()
    return Triple(first.name, first.minutes + 1440, sorted.last().minutes)
}

private fun formatRemaining(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) {
        "${bnNumber(h)} ঘণ্টা ${bnNumber(m)} মিনিট বাকি"
    } else {
        "${bnNumber(m)} মিনিট বাকি"
    }
}
