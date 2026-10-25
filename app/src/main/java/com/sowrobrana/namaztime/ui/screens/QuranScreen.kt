package com.sowrobrana.namaztime.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sowrobrana.namaztime.data.Prefs
import com.sowrobrana.namaztime.ui.quran.QuranAudio
import com.sowrobrana.namaztime.ui.quran.QuranData
import com.sowrobrana.namaztime.ui.quran.QuranPara
import com.sowrobrana.namaztime.ui.quran.QuranSurah
import com.sowrobrana.namaztime.util.bnNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface Reader {
    data class Surah(val id: Int, val ayah: Int) : Reader
    data class Para(val num: Int) : Reader
}

@Composable
fun QuranScreen() {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    var loaded by remember { mutableStateOf(QuranData.loaded) }
    var mode by remember { mutableStateOf(0) } // 0 = সূরা, 1 = পারা
    var query by remember { mutableStateOf("") }
    var reader by remember { mutableStateOf<Reader?>(null) }

    if (!loaded) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) { QuranData.load(context) }
            loaded = true
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    when (val r = reader) {
        is Reader.Surah -> {
            val surah = QuranData.surah(r.id) ?: return
            BackHandler { reader = null }
            SurahReader(
                surah = surah,
                startAyah = r.ayah,
                onBack = { reader = null },
                onSave = { ayah ->
                    prefs.lastQuranSurah = surah.id
                    prefs.lastQuranAyah = ayah
                },
            )
        }
        is Reader.Para -> {
            val para = QuranData.paras.firstOrNull { it.num == r.num } ?: return
            BackHandler { reader = null }
            ParaReader(
                para = para,
                onBack = { reader = null },
                onSave = { s, a ->
                    prefs.lastQuranSurah = s
                    prefs.lastQuranAyah = a
                },
            )
        }
        null -> {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "আল-কুরআন",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp),
                )
                // Resume card.
                val lastSurah = prefs.lastQuranSurah
                val lastAyah = prefs.lastQuranAyah
                if (lastSurah > 0) {
                    val s = QuranData.surah(lastSurah)
                    if (s != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 4.dp, 16.dp, 8.dp)
                                .clickable {
                                    reader = Reader.Surah(lastSurah, lastAyah)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF0B3D2E),
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "শেষ পড়া থেকে চালিয়ে যান",
                                    color = Color(0xFFD4AF37),
                                    fontSize = 13.sp,
                                )
                                Text(
                                    text = "সূরা ${s.nameBn} • আয়াত ${bnNumber(lastAyah)}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = mode == 0,
                        onClick = { mode = 0 },
                        label = { Text("সূরা (১১৪)") },
                    )
                    FilterChip(
                        selected = mode == 1,
                        onClick = { mode = 1 },
                        label = { Text("পারা (৩০)") },
                    )
                }
                if (mode == 0) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("সূরা খুঁজুন (নাম বা নম্বর)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp, 16.dp, 4.dp),
                    )
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (mode == 0) {
                        val q = query.trim()
                        val list = QuranData.surahs.filter { s ->
                            q.isEmpty() ||
                                s.nameBn.contains(q) ||
                                bnNumber(s.id) == q ||
                                s.id.toString() == q
                        }
                        items(list) { s ->
                            SurahRow(s) { reader = Reader.Surah(s.id, 1) }
                        }
                    } else {
                        items(QuranData.paras) { p ->
                            ParaRow(p) { reader = Reader.Para(p.num) }
                        }
                    }
                    item {
                        Text(
                            text = "বাংলা উচ্চারণ সহায়ক মাত্র — শুদ্ধ তিলাওয়াতের " +
                                "জন্য আরবি পাঠকেই মূল ধরুন।\n" +
                                "আরবি পাঠ: উসমানি মুসহাফ • বাংলা অর্থ: মুহিউদ্দিন খান • " +
                                "শব্দার্থ: ওয়ার্ড-বাই-ওয়ার্ড কুরআন।",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahRow(s: QuranSurah, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 4.dp, 16.dp, 4.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF0B3D2E), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bnNumber(s.id),
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = s.nameBn, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${s.type} • ${bnNumber(s.verses.size)} আয়াত",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(text = s.nameAr, fontSize = 20.sp, color = Color(0xFF0B3D2E))
        }
    }
}

@Composable
private fun ParaRow(p: QuranPara, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 4.dp, 16.dp, 4.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF0B3D2E), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = bnNumber(p.num),
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "পারা ${bnNumber(p.num)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${bnNumber(p.count)} আয়াত",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SurahReader(
    surah: QuranSurah,
    startAyah: Int,
    onBack: () -> Unit,
    onSave: (Int) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = (startAyah - 1).coerceIn(0, surah.verses.size - 1),
    ) { surah.verses.size }
    val playingSurah by QuranAudio.playingSurah
    val bismillah = QuranData.bismillah

    LaunchedEffect(pagerState.currentPage) {
        onSave(pagerState.currentPage + 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 8.dp, 8.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "ফিরুন")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "সূরা ${surah.nameBn}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${surah.type} • ${bnNumber(surah.verses.size)} আয়াত",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
            if (surah.id in QuranData.audioSurahs) {
                IconButton(onClick = { QuranAudio.toggle(context, surah.id) }) {
                    Icon(
                        if (playingSurah == surah.id) Icons.Filled.Pause
                        else Icons.Filled.PlayArrow,
                        contentDescription = "তেলাওয়াত",
                    )
                }
            }
        }
        if (surah.id != 9 && bismillah != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E7C3)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = bismillah.arabic,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                    )
                    Text(text = bismillah.translit, fontSize = 15.sp)
                    Text(
                        text = bismillah.meaning,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            AyahPage(
                surahId = surah.id,
                ayah = page + 1,
                arabic = surah.verses[page].arabic,
                translit = surah.verses[page].translit,
                meaning = surah.verses[page].meaning,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp, 16.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                enabled = pagerState.currentPage > 0,
            ) { Text("আগের আয়াত") }
            Text(
                text = "${bnNumber(pagerState.currentPage + 1)} / ${bnNumber(surah.verses.size)}",
                fontWeight = FontWeight.Bold,
            )
            Button(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                enabled = pagerState.currentPage < surah.verses.size - 1,
            ) { Text("পরের আয়াত") }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParaReader(
    para: QuranPara,
    onBack: () -> Unit,
    onSave: (Int, Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val items = remember(para) { QuranData.paraItems(para) }
    val pagerState = rememberPagerState { items.size }

    LaunchedEffect(pagerState.currentPage) {
        val (s, idx) = items[pagerState.currentPage]
        onSave(s.id, idx + 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 8.dp, 8.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "ফিরুন")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "পারা ${bnNumber(para.num)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${bnNumber(para.count)} আয়াত",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            val (surah, idx) = items[page]
            val verse = surah.verses[idx]
            val isSurahStart = idx == 0
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                if (isSurahStart) {
                    Text(
                        text = "সূরা ${surah.nameBn}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B3D2E),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    if (surah.id != 9) {
                        QuranData.bismillah?.let { b ->
                            Text(
                                text = b.arabic,
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                text = b.translit,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                AyahPageContent(
                    surahId = surah.id,
                    ayah = idx + 1,
                    arabic = verse.arabic,
                    translit = verse.translit,
                    meaning = verse.meaning,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp, 16.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                enabled = pagerState.currentPage > 0,
            ) { Text("আগের আয়াত") }
            Text(
                text = "${bnNumber(pagerState.currentPage + 1)} / ${bnNumber(items.size)}",
                fontWeight = FontWeight.Bold,
            )
            Button(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                enabled = pagerState.currentPage < items.size - 1,
            ) { Text("পরের আয়াত") }
        }
    }
}

@Composable
private fun AyahPage(
    surahId: Int,
    ayah: Int,
    arabic: String,
    translit: String,
    meaning: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        AyahPageContent(surahId, ayah, arabic, translit, meaning)
    }
}

@Composable
private fun AyahPageContent(
    surahId: Int,
    ayah: Int,
    arabic: String,
    translit: String,
    meaning: String,
) {
    val context = LocalContext.current
    var showWords by remember { mutableStateOf(false) }
    var words by remember {
        mutableStateOf<List<Pair<String, String>>?>(null)
    }

    Text(
        text = "আয়াত ${bnNumber(ayah)}",
        fontSize = 13.sp,
        color = Color(0xFF0B3D2E),
        fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = arabic,
        fontSize = 27.sp,
        lineHeight = 42.sp,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = translit,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        color = Color(0xFF333333),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = meaning,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        color = Color.Gray,
    )
    Spacer(modifier = Modifier.height(16.dp))
    TextButton(onClick = {
        if (!showWords && words == null) {
            words = QuranData.words(context, surahId, ayah)
        }
        showWords = !showWords
    }) {
        Text(if (showWords) "শব্দার্থ লুকান" else "শব্দার্থ দেখুন")
    }
    if (showWords) {
        val list = words.orEmpty()
        if (list.isEmpty()) {
            Text(
                text = "এই আয়াতের শব্দার্থ পাওয়া যায়নি।",
                fontSize = 13.sp,
                color = Color.Gray,
            )
        } else {
            Column {
                for ((ar, bn) in list) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = ar, fontSize = 17.sp)
                        Text(
                            text = bn,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
