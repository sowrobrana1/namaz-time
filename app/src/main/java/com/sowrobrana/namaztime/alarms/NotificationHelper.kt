package com.sowrobrana.namaztime.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sowrobrana.namaztime.AzanAlarmActivity
import com.sowrobrana.namaztime.R

object NotificationHelper {

    const val CHANNEL_ID = "prayer_channel"

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
        ensureChannel(context)
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
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("$subtitle — বন্ধ করতে ট্যাপ করুন")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pending, true)
            .setContentIntent(pending)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
        manager.notify(id, notification)
    }
}
