package com.sowrobrana.namaztime.alarms

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.sowrobrana.namaztime.R

/**
 * Single azan player shared by the alarm activity.
 *
 * Only one azan ever plays at a time: the activity cancels the
 * notification (which stops the notification-channel sound) and then
 * starts this player, so the two never overlap. Audio attributes are
 * set BEFORE prepare (the correct order) with USAGE_ALARM, and alarm
 * audio focus is requested so a playing video ducks/pauses instead of
 * drowning the azan out.
 */
object AzanPlayer {

    private var player: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    fun isPlaying(): Boolean = try {
        player?.isPlaying == true
    } catch (_: Exception) {
        false
    }

    fun start(context: Context) {
        if (isPlaying()) return
        try {
            val appContext = context.applicationContext
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attrs)
                    .build()
                am.requestAudioFocus(req)
                focusRequest = req
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    null,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
                )
            }
            audioManager = am
            val p = MediaPlayer()
            p.setAudioAttributes(attrs)
            val afd = appContext.resources.openRawResourceFd(R.raw.azan)
            p.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            p.setOnCompletionListener { stop() }
            p.prepare()
            p.start()
            player = p
        } catch (_: Exception) {
            player = null
        }
    }

    fun stop() {
        try {
            player?.stop()
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        } catch (_: Exception) {
        }
        focusRequest = null
        audioManager = null
    }
}
