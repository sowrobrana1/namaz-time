package com.sowrobrana.namaztime.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.sowrobrana.namaztime.AzanAlarmActivity
import com.sowrobrana.namaztime.R

object NotificationHelper {

    const val CHANNEL_ID = "prayer_channel"

    /** Azan alarm channel: plays the bundled azan as the notification
     * sound itself, so the azan is heard even when the full-screen
     * activity does not launch (e.g. the screen is on and the system
     * shows only a heads-up notification). The activity cancels this
     * notification on open and plays the azan itself instead. */
    const val AZAN_CHANNEL_ID = "azan_alarm_channel"

    fun ensureAzanChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(AZAN_CHANNEL_ID) == null) {
            val soundUri = Uri.parse(
                "android.resource://${context.packageName}/raw/azan",
            )
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val channel = NotificationChannel(
                AZAN_CHANNEL_ID,
                "আজানের অ্যালার্ম",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "প্রতিটি ওয়াক্তে আজান বাজবে"
                setSound(soundUri, audioAttrs)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "নামাজের সময়",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "প্রতিটি ওয়াক্তের শুরুতে নোটিফিকেশন"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showPrayerNotification(context: Context, prayerName: String, id: Int) {
        ensureChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$prayerName-এর সময় হয়েছে")
            .setContentText("এখন $prayerName-এর ওয়াক্ত চলছে")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    /**
     * Azan alarm: a high-priority notification whose full-screen intent
     * opens [AzanAlarmActivity] over the lock screen — that activity
     * plays the azan audio and stops on tap, swipe, or power button.
     * Tapping the notification itself also opens the activity.
     */
    fun showAzanAlarm(context: Context, title: String, subtitle: String, id: Int) {
        ensureAzanChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, AzanAlarmActivity::class.java).apply {
            putExtra("alarm_title", title)
            putExtra("alarm_subtitle", subtitle)
            putExtra("notif_id", id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, AZAN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("$subtitle — বন্ধ করতে ট্যাপ করুন")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pending, true)
            .setContentIntent(pending)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }
}
