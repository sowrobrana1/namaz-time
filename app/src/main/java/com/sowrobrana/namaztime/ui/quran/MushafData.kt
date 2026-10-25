package com.sowrobrana.namaztime.ui.quran

import android.content.Context
import org.json.JSONObject

/** One item on a Mushaf page, in reading order. */
sealed interface MushafItem {
    /** Ornate surah header frame (surah number). */
    data class Header(val surah: Int) : MushafItem

    /** Centered Bismillah line at a surah start (except surahs 1 and 9). */
    object Bismillah : MushafItem

    /** One ayah of Uthmani Arabic text. */
    data class Ayah(val surah: Int, val ayah: Int, val text: String) : MushafItem
}

data class MushafPage(val num: Int, val items: List<MushafItem>)

/**
 * Mushaf page data (Madani pagination, 604 pages), loaded lazily from
 * assets/quran/pages.json. Same Uthmani Arabic text as the ayah reader,
 * re-laid-out page by page; no new text content is bundled.
 */
object MushafData {

    var names: Map<Int, String> = emptyMap()
        private set
    var pages: List<MushafPage> = emptyList()
        private set
    var loaded = false
        private set

    fun load(context: Context) {
        if (loaded) return
        val root = JSONObject(
            context.assets.open("quran/pages.json").bufferedReader().readText(),
        )
        val nMap = mutableMapOf<Int, String>()
        val nObj = root.getJSONObject("names")
        for (key in nObj.keys()) {
            nMap[key.toInt()] = nObj.getString(key)
        }
        names = nMap
        val pArr = root.getJSONArray("pages")
        val list = mutableListOf<MushafPage>()
        for (i in 0 until pArr.length()) {
            val p = pArr.getJSONObject(i)
            val iArr = p.getJSONArray("items")
            val items = mutableListOf<MushafItem>()
            for (j in 0 until iArr.length()) {
                val it = iArr.getJSONObject(j)
                when {
                    it.has("h") -> items.add(MushafItem.Header(it.getInt("h")))
                    it.has("b") -> items.add(MushafItem.Bismillah)
                    else -> items.add(
                        MushafItem.Ayah(
                            it.getInt("s"),
                            it.getInt("a"),
                            it.getString("t"),
                        ),
                    )
                }
            }
            list.add(MushafPage(p.getInt("n"), items))
        }
        pages = list
        loaded = true
    }
}
