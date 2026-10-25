package com.sowrobrana.namaztime.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sowrobrana.namaztime.alarms.NotificationHelper
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.Hijri
import com.sowrobrana.namaztime.ui.NamazViewModel
import com.sowrobrana.namaztime.ui.PrayerRow
import com.sowrobrana.namaztime.util.BN_MONTHS
import com.sowrobrana.namaztime.util.BN_WEEKDAYS
import com.sowrobrana.namaztime.util.LocationHelper
import com.sowrobrana.namaztime.util.bnDigits
import com.sowrobrana.namaztime.util.bnNumber
import com.sowrobrana.namaztime.util.formatTimeBn
import java.util.Calendar

@Composable
fun TimesScreen(vm: NamazViewModel, onRequestExactAlarm: () -> Unit) {

    val locationMode by vm.locationMode.collectAsState()
    val autoLabel by vm.autoLabel.collectAsState()
    val city by vm.effectiveCity.collectAsState()
    val mazhab by vm.mazhab.collectAsState()
    val notifyEnabled by vm.notifyEnabled.collectAsState()
    val rows by vm.todayRows.collectAsState()
    val tomorrowMaghrib by vm.tomorrowMaghrib.collectAsState()
    val hijri by vm.hijri.collectAsState()
    val now by vm.nowMinutes.collectAsState()
    val context = LocalContext.current

    // Auto-location: permission -> GPS fix -> times follow the new place.
    var locStatus by remember { mutableStateOf("") } // "", "searching", "notfound"
    var locDenied by remember { mutableStateOf(false) }
    fun fetchAndApply(onDone: () -> Unit = {}) {
        locStatus = "searching"
        LocationHelper.fetchCurrent(
            context,
            onResult = { lat, lon ->
                vm.setAutoLocation(lat, lon)
                locStatus = ""
                onDone()
            },
            onEmpty = { locStatus = "notfound" },
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            locDenied = false
            fetchAndApply()
        } else {
            locDenied = true
        }
    }
    fun startAuto() {
        if (LocationHelper.hasPermission(context)) {
            fetchAndApply()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    // Silent refresh when auto mode is on: moving to a new place updates the times.
    LaunchedEffect(locationMode) {
        if (locationMode == "auto" && LocationHelper.hasPermission(context)) {
            LocationHelper.fetchCurrent(
                context,
                onResult = { lat, lon -> vm.setAutoLocation(lat, lon) },
                onEmpty = {},
            )
        }
    }

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

        // Location card (Muslims Day style): auto GPS vs manual district.
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "লোকেশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = locationMode == "auto",
                        onClick = {
                            if (locationMode != "auto") {
                                vm.setLocationMode("auto")
                                startAuto()
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("অটোমেটিক পদ্ধতি")
                                Text(
                                    text = "  নির্ভুল সময়",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB8860B),
                                )
                            }
                        },
                    )
                    FilterChip(
                        selected = locationMode != "auto",
                        onClick = { vm.setLocationMode("manual") },
                        label = { Text("ম্যানুয়াল পদ্ধতি") },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (locationMode == "auto") {
                    if (!LocationHelper.hasPermission(context)) {
                        Text(
                            text = "লোকেশন পারমিশন দিলে, আপনার বর্তমান অবস্থান অনুযায়ী আরও নির্ভুল সময়সূচি দেখানো হবে।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { startAuto() }) {
                            Text("লোকেশন পারমিশন দিন")
                        }
                        if (locDenied) {
                            Text(
                                text = "পারমিশন দেওয়া হয়নি — ফোনের সেটিংস থেকে লোকেশন চালু করে আবার চেষ্টা করুন, অথবা ম্যানুয়াল জেলা বেছে নিন।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else if (locStatus == "searching") {
                        Text(text = "বর্তমান অবস্থান খুঁজছি…", fontSize = 14.sp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "বর্তমান অবস্থান",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = autoLabel.ifEmpty { city.name },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            IconButton(onClick = { fetchAndApply() }) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = "লোকেশন আবার খুঁজুন",
                                )
                            }
                        }
                        if (locStatus == "notfound") {
                            Text(
                                text = "লোকেশন পাওয়া যায়নি — GPS চালু করে আবার চেষ্টা করুন, অথবা ম্যানুয়াল জেলা বেছে নিন।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        Text(
                            text = "অটোমেটিক লোকেশনে ইসলামিক ফাউন্ডেশনের সময়ের সাথে ১-২ মিনিট পার্থক্য হতে পারে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "বর্তমান জেলা: ${city.name}",
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { cityDialogOpen = true }) {
                            Text("জেলা বেছে নিন")
                        }
                    }
                }
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
        Text(
            text = "চালু থাকলে প্রতিটি ওয়াক্তে আজান বাজবে আর লক স্ক্রিনে আজানের পর্দা আসবে।",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 4.dp),
        )
        // Azan test: plays the alarm immediately so the user can verify
        // it works without waiting for the next prayer time.
        val ctx = LocalContext.current
        TextButton(
            onClick = {
                NotificationHelper.showAzanAlarm(
                    ctx,
                    "আজান পরীক্ষা",
                    "পরীক্ষামূলক আজান বাজছে",
                    9999,
                )
            },
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 4.dp),
        ) {
            Text("আজান পরীক্ষা করুন", fontSize = 13.sp)
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

        // Ramadan card: sehri end (= Fajr) and iftar (= Maghrib) times.
        // The Fajr azan alarm doubles as the sehri-end alarm and the
        // Maghrib azan alarm doubles as the iftar alarm (see Receivers).
        if (Hijri.fromGregorian(
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH),
            ).month == 9
        ) {
            val fajrMin = rows.firstOrNull { it.name == "ফজর" }?.minutes
            val maghribMin = rows.firstOrNull { it.name == "মাগরিব" }?.minutes
            if (fajrMin != null && maghribMin != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp, 16.dp, 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "রমজান",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                        )
                        Text(
                            text = "সেহরির শেষ সময়: ${formatTimeBn(fajrMin)}",
                            fontSize = 15.sp,
                        )
                        Text(
                            text = "ইফতার: ${formatTimeBn(maghribMin)}",
                            fontSize = 15.sp,
                        )
                        Text(
                            text = "ফজরের আজানই সেহরির শেষ সময়ের অ্যালার্ম, মাগরিবের আজানই ইফতারের অ্যালার্ম।",
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }

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
        // Forbidden windows + nafl times + next sehri/iftar (computed from today's times).
        val fajrMin = rows.firstOrNull { it.name == "ফজর" }?.minutes
        val sunriseMin = rows.firstOrNull { it.name == "সূর্যোদয়" }?.minutes
        val dhuhrMin = rows.firstOrNull { it.name == "যোহর" }?.minutes
        val maghribMin = rows.firstOrNull { it.name == "মাগরিব" }?.minutes
        val ishaMin = rows.firstOrNull { it.name == "এশা" }?.minutes
        val tomorrowFajrMin = rows.firstOrNull { it.name == "এশা" }?.windowEnd
        if (fajrMin != null && sunriseMin != null && dhuhrMin != null &&
            maghribMin != null && ishaMin != null && tomorrowFajrMin != null &&
            tomorrowFajrMin > 0
        ) {
            val nightLen = (tomorrowFajrMin + 1440) - maghribMin
            val lastThirdMin = ((tomorrowFajrMin - nightLen / 3) % 1440 + 1440) % 1440
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "সালাতের নিষিদ্ধ সময়",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9B1C1C),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("সকাল: ${formatTimeBn(sunriseMin)} - ${formatTimeBn(sunriseMin + 15)}", fontSize = 14.sp)
                    Text("দুপুর: ${formatTimeBn(dhuhrMin - 7)} - ${formatTimeBn(dhuhrMin)}", fontSize = 14.sp)
                    Text("সন্ধ্যা: ${formatTimeBn(maghribMin - 14)} - ${formatTimeBn(maghribMin)}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "এই সময়গুলোতে সালাত আদায় নিষিদ্ধ (ব্যতিক্রম: সূর্যাস্তকালীন একই দিনের আসর সালাত)।",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "নফল সালাতের সময়",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("দুহা (চাশত): ${formatTimeBn(sunriseMin + 15)} - ${formatTimeBn(dhuhrMin - 7)}", fontSize = 14.sp)
                    Text("জাওয়াল শুরু: ${formatTimeBn(dhuhrMin)}", fontSize = 14.sp)
                    Text("আওয়াবিন: ${formatTimeBn(maghribMin)} - ${formatTimeBn(ishaMin)}", fontSize = 14.sp)
                    Text("তাহাজ্জুদ: ${formatTimeBn(ishaMin)} - ${formatTimeBn(tomorrowFajrMin)}", fontSize = 14.sp)
                    Text(
                        text = "রাতের শেষ ১/৩ শুরু: ${formatTimeBn(lastThirdMin)} (তাহাজ্জুদের উত্তম সময়)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val nextIftarMin = if (now < maghribMin) maghribMin else tomorrowMaghrib
            val sehriLeftMin = (tomorrowFajrMin + 1440) - now
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E7C3)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "পরবর্তী সাহরি ও ইফতার",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("পরবর্তী সাহরি: ${formatTimeBn(tomorrowFajrMin)}", fontSize = 14.sp)
                    Text("পরবর্তী ইফতার: ${formatTimeBn(nextIftarMin)}", fontSize = 14.sp)
                    Text(
                        text = "সাহরির বাকি: ${sehriLeftMin / 60} ঘণ্টা ${sehriLeftMin % 60} মিনিট",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
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
