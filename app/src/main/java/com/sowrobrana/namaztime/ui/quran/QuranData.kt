package com.sowrobrana.namaztime.ui.quran

import android.content.Context
import org.json.JSONObject

/** One ayah: Arabic (Uthmani) + Bangla uchcharon + Bangla meaning. */
data class QuranVerse(
    val arabic: String,
    val translit: String,
    val meaning: String,
)

data class QuranSurah(
    val id: Int,
    val nameBn: String,
    val nameAr: String,
    val type: String,
    val verses: List<QuranVerse>,
)

data class QuranPara(
    val num: Int,
    val startSurah: Int,
    val startAyah: Int,
    val endSurah: Int,
    val endAyah: Int,
    val count: Int,
)

data class Bismillah(
    val arabic: String,
    val translit: String,
    val meaning: String,
)

/**
 * Offline Quran data, loaded once from the JSON files under assets/quran.
 *
 * Sources (credited on the Quran home screen): Uthmani Arabic text,
 * Bangla translation (Muhiuddin Khan), Bangla word meanings, and
 * Bangla uchcharon generated from the standard Latin transliteration.
 */
object QuranData {

    var surahs: List<QuranSurah> = emptyList()
        private set
    var paras: List<QuranPara> = emptyList()
        private set
    var bismillah: Bismillah? = null
        private set
    var audioSurahs: Set<Int> = emptySet()
        private set
    var loaded = false
        private set

    private var wordsMap: Map<String, List<Pair<String, String>>>? = null

    fun load(context: Context) {
        if (loaded) return
        val quran = JSONObject(
            context.assets.open("quran/quran.json").bufferedReader().readText(),
        )
        val b = quran.getJSONObject("bismillah")
        bismillah = Bismillah(b.getString("a"), b.getString("t"), b.getString("b"))
        val sList = mutableListOf<QuranSurah>()
        val sArr = quran.getJSONArray("surahs")
        for (i in 0 until sArr.length()) {
            val s = sArr.getJSONObject(i)
            val vArr = s.getJSONArray("verses")
            val vs = mutableListOf<QuranVerse>()
            for (j in 0 until vArr.length()) {
                val v = vArr.getJSONObject(j)
                vs.add(QuranVerse(v.getString("a"), v.getString("t"), v.getString("b")))
            }
            sList.add(
                QuranSurah(
                    id = s.getInt("id"),
                    nameBn = s.getString("bn"),
                    nameAr = s.getString("ar"),
                    type = s.getString("type"),
                    verses = vs,
                ),
            )
        }
        surahs = sList
        val pList = mutableListOf<QuranPara>()
        val pArr = quran.getJSONArray("paras")
        for (i in 0 until pArr.length()) {
            val p = pArr.getJSONObject(i)
            val st = p.getJSONArray("start")
            val en = p.getJSONArray("end")
            pList.add(
                QuranPara(
                    num = p.getInt("p"),
                    startSurah = st.getInt(0),
                    startAyah = st.getInt(1),
                    endSurah = en.getInt(0),
                    endAyah = en.getInt(1),
                    count = p.getInt("count"),
                ),
            )
        }
        paras = pList
        audioSurahs = try {
            context.assets.list("quran/audio").orEmpty()
                .mapNotNull { it.removeSuffix(".ogg").toIntOrNull() }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
        loaded = true
    }

    fun surah(id: Int): QuranSurah? = surahs.firstOrNull { it.id == id }

    /** Flat (surah, ayahIndex) list for a para, in reading order. */
    fun paraItems(para: QuranPara): List<Pair<QuranSurah, Int>> {
        val out = mutableListOf<Pair<QuranSurah, Int>>()
        for (s in surahs) {
            if (s.id < para.startSurah || s.id > para.endSurah) continue
            for ((idx, _) in s.verses.withIndex()) {
                val ayah = idx + 1
                if (s.id == para.startSurah && ayah < para.startAyah) continue
                if (s.id == para.endSurah && ayah > para.endAyah) continue
                out.add(s to idx)
            }
        }
        return out
    }

    /** Bangla word meanings for one ayah: (arabic word, bangla meaning). */
    fun words(context: Context, surahId: Int, ayah: Int): List<Pair<String, String>> {
        if (wordsMap == null) {
            val map = mutableMapOf<String, List<Pair<String, String>>>()
            try {
                val root = JSONObject(
                    context.assets.open("quran/words.json")
                        .bufferedReader().readText(),
                )
                for (key in root.keys()) {
                    val arr = root.getJSONArray(key)
                    val list = mutableListOf<Pair<String, String>>()
                    for (i in 0 until arr.length()) {
                        val w = arr.getJSONArray(i)
                        list.add(w.getString(0) to w.getString(1))
                    }
                    map[key] = list
                }
            } catch (_: Exception) {
            }
            wordsMap = map
        }
        return wordsMap?.get("$surahId:$ayah").orEmpty()
    }
}
