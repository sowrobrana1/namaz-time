package com.sowrobrana.namaztime.ui.quran

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf

/**
 * Per-surah tilawat player (Mishary Alafasy, bundled Opus files in
 * assets/quran/audio/NNN.ogg). One surah at a time; toggling the same
 * surah stops it.
 */
object QuranAudio {

    /** Currently playing surah id, or 0 when idle. Observed by Compose. */
    val playingSurah = mutableStateOf(0)

    private var player: MediaPlayer? = null

    /** Returns true if playback started, false if the file is missing. */
    fun toggle(context: Context, surahId: Int): Boolean {
        if (playingSurah.value == surahId) {
            stop()
            return false
        }
        stop()
        return try {
            val fd = context.assets.openFd(
                "quran/audio/%03d.ogg".format(surahId),
            )
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                fd.close()
                setOnCompletionListener { stop() }
                prepare()
                start()
            }
            playingSurah.value = surahId
            true
        } catch (_: Exception) {
            stop()
            false
        }
    }

    fun stop() {
        try {
            player?.stop()
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        playingSurah.value = 0
    }
}
