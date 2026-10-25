package com.sowrobrana.namaztime.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sowrobrana.namaztime.R
import com.sowrobrana.namaztime.ui.quran.MushafData
import com.sowrobrana.namaztime.ui.quran.MushafItem
import com.sowrobrana.namaztime.ui.quran.MushafPage
import com.sowrobrana.namaztime.ui.quran.QuranData
import com.sowrobrana.namaztime.util.bnNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val amiriFamily = FontFamily(Font(R.font.amiri))

/** Western digits -> Eastern Arabic digits used in Mushaf ayah markers. */
private fun easternArabic(n: Int): String {
    val digits = "٠١٢٣٤٥٦٧٨٩"
    return n.toString().map { digits[it - '0'] }.joinToString("")
}

/**
 * Mushaf page reader: Arabic only, one printed page per swipe, in the
 * Madani 604-page layout. Meant for readers who read Arabic and do
 * khatam; the ayah-by-ayah reader (with uchcharon + meaning) stays
 * as the other mode.
 */
@Composable
fun MushafReader(
    startPage: Int,
    onBack: () -> Unit,
    onSave: (Int) -> Unit,
) {
    val context = LocalContext.current
    var loaded by remember { mutableStateOf(MushafData.loaded) }

    if (!loaded) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) { MushafData.load(context) }
            loaded = true
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val pages = MushafData.pages
    if (pages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("মুসহাফ ডাটা পাওয়া যায়নি")
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = (startPage - 1).coerceIn(0, pages.size - 1),
    ) { pages.size }

    LaunchedEffect(pagerState.currentPage) {
        onSave(pagerState.currentPage + 1)
    }

    BackHandler { onBack() }
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
                    text = "মুসহাফ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "পাতা ${bnNumber(pagerState.currentPage + 1)} / " +
                        "${bnNumber(pages.size)} • শুধু আরবি",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            MushafPageView(page = pages[index])
        }
    }
}

@Composable
private fun MushafPageView(page: MushafPage) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF5))
            .verticalScroll(scroll)
            .padding(20.dp, 12.dp, 20.dp, 16.dp),
    ) {
        // Split the page into blocks: headers / bismillah lines break
        // the continuous ayah text into separate paragraphs.
        val ayahRun = StringBuilder()
        @Composable
        fun flushAyahRun() {
            if (ayahRun.isNotEmpty()) {
                Text(
                    text = ayahRun.toString().trim(),
                    fontFamily = amiriFamily,
                    fontSize = 25.sp,
                    lineHeight = 42.sp,
                    textAlign = TextAlign.Right,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.fillMaxWidth(),
                )
                ayahRun.clear()
            }
        }
        for (item in page.items) {
            when (item) {
                is MushafItem.Header -> {
                    flushAyahRun()
                    val name = MushafData.names[item.surah] ?: ""
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp, 0.dp, 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEFF6F0),
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp, 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "سورة $name",
                                fontFamily = amiriFamily,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B3D2E),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                is MushafItem.Bismillah -> {
                    flushAyahRun()
                    // Bismillah text comes from the DB via QuranData (loaded
                    // with the Quran tab) — never hand-typed Arabic.
                    val bText = QuranData.bismillah?.arabic ?: ""
                    if (bText.isNotEmpty()) {
                        Text(
                            text = bText,
                            fontFamily = amiriFamily,
                            fontSize = 23.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(0.dp, 4.dp, 0.dp, 8.dp),
                        )
                    }
                }
                is MushafItem.Ayah -> {
                    ayahRun.append(item.text)
                    ayahRun.append(" ۝${easternArabic(item.ayah)} ")
                }
            }
        }
        flushAyahRun()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "পাতা ${bnNumber(page.num)}",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
