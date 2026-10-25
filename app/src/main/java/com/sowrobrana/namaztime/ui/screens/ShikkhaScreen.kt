package com.sowrobrana.namaztime.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sowrobrana.namaztime.ui.shikkha.DUA_QUNOOT_ARABIC
import com.sowrobrana.namaztime.ui.shikkha.DUA_QUNOOT_MEANING
import com.sowrobrana.namaztime.ui.shikkha.DuaItem
import com.sowrobrana.namaztime.ui.shikkha.PrayerGuide
import com.sowrobrana.namaztime.ui.shikkha.duaCategories
import com.sowrobrana.namaztime.ui.shikkha.duas
import com.sowrobrana.namaztime.ui.shikkha.prayerGuides

private val DeepEmerald = Color(0xFF0B3D2E)
private val Gold = Color(0xFFD4AF37)

@Composable
fun ShikkhaScreen() {
    var tab by remember { mutableStateOf(0) }
    var selectedGuide by remember { mutableStateOf<PrayerGuide?>(null) }

    val guide = selectedGuide
    if (guide != null) {
        GuideDetailScreen(guide = guide, onBack = { selectedGuide = null })
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("শিক্ষা", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "নামাজের নিয়ম ধাপে ধাপে, আর দৈনন্দিন দোয়া — কখন কোনটা পড়তে হয়, সব এখানে।",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tab == 0,
                onClick = { tab = 0 },
                label = { Text("নামাজ শিক্ষা") },
            )
            FilterChip(
                selected = tab == 1,
                onClick = { tab = 1 },
                label = { Text("দোয়া") },
            )
        }
        Spacer(Modifier.height(12.dp))
        if (tab == 0) {
            PrayerGuideList(onSelect = { selectedGuide = it })
        } else {
            DuaList()
        }
    }
}

@Composable
private fun PrayerGuideList(onSelect: (PrayerGuide) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "হানাফি মতে লেখা",
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "নিয়ত মনের সংকল্প — মুখে বলা জরুরি নয়। প্রতিটা ধাপে ছবি দেখে শিখুন: কিভাবে হাত বাঁধতে হয়, রুকু কেমন হয়, সিজদা কেমন হয়।",
                        color = Color.White,
                        fontSize = 13.sp,
                    )
                }
            }
        }
        items(prayerGuides) { guide ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(guide) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${guide.name} নামাজের শিক্ষা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            guide.rakats,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun GuideDetailScreen(guide: PrayerGuide, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                }
                Column {
                    Text(
                        "${guide.name} নামাজের শিক্ষা",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        guide.rakats,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(guide.steps) { step ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(step.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    step.posture.drawable?.let { res ->
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(DeepEmerald),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(res),
                                contentDescription = step.title,
                                modifier = Modifier.size(200.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(step.body, fontSize = 14.sp)
                    step.recitation?.let { arabic ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            arabic,
                            fontSize = 19.sp,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    step.meaning?.let { meaning ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            meaning,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (guide.name == "এশা") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepEmerald),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "দোয়া কুনুত (বিতরের ৩য় রাকাতে)",
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "৩য় রাকাতে ফাতিহা ও সূরা পড়ার পর আল্লাহু আকবর বলে হাত বেঁধে এই দোয়া পড়ুন, তারপর রুকুতে যান।",
                            color = Color.White,
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            DUA_QUNOOT_ARABIC,
                            color = Color.White,
                            fontSize = 19.sp,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            DUA_QUNOOT_MEANING,
                            color = Color(0xFFD8E8DF),
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
        items(guide.notes) { note ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "• $note",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun DuaList() {
    var category by remember { mutableStateOf("সব") }
    val shown = if (category == "সব") duas else duas.filter { it.category == category }
    Column {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            duaCategories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "${shown.size}টি দোয়া",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(shown) { dua -> DuaCard(dua) }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun DuaCard(dua: DuaItem) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(dua.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        dua.occasion,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Text(
                    dua.arabic,
                    fontSize = 19.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    dua.meaning,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
