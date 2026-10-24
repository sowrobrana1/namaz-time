package com.sowrobrana.namaztime.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
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
}
