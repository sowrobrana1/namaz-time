package com.sowrobrana.namaztime

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sowrobrana.namaztime.alarms.AzanPlayer

/**
 * Full-screen azan alarm screen.
 *
 * Shows over the lock screen at prayer time, plays the bundled azan
 * audio, and stops when the user taps the button, swipes down, or
 * presses the power button (screen off -> onPause -> dismiss).
 */
class AzanAlarmActivity : ComponentActivity() {

    private var notifId: Int = -1
    private var dismissed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Wake the screen and show over the lock screen.
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        )
        val prayerName = intent.getStringExtra("prayer_name") ?: "নামাজ"
        val alarmTitle = intent.getStringExtra("alarm_title") ?: "$prayerName-এর আজান"
        val alarmSubtitle =
            intent.getStringExtra("alarm_subtitle") ?: "এখন $prayerName-এর ওয়াক্ত শুরু হয়েছে"
        notifId = intent.getIntExtra("notif_id", -1)
        // Cancel first: this stops the notification-channel azan sound,
        // then the shared player plays the azan itself — never both.
        if (notifId >= 0) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notifId)
        }
        AzanPlayer.start(this)
        setContent {
            MaterialTheme {
                AzanAlarmScreen(
                    title = alarmTitle,
                    subtitle = alarmSubtitle,
                    onDismiss = { dismiss() },
                )
            }
        }
    }

    /** Power button (screen off) lands here too — stop the azan. */
    override fun onPause() {
        super.onPause()
        if (!isFinishing) dismiss()
    }

    override fun onDestroy() {
        AzanPlayer.stop()
        super.onDestroy()
    }

    private fun dismiss() {
        if (dismissed) return
        dismissed = true
        AzanPlayer.stop()
        if (notifId >= 0) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notifId)
        }
        finish()
    }
}

@Composable
private fun AzanAlarmScreen(title: String, subtitle: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B3D2E))
            .pointerInput(Unit) {
                // Swipe down anywhere also dismisses the alarm.
                var total = 0f
                detectVerticalDragGestures { _, dragAmount ->
                    total += dragAmount
                    if (total > 200f) onDismiss()
                }
            }
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "আজান",
                fontSize = 20.sp,
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 34.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = Color(0xFFB0B0B0),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = "বন্ধ করতে নিচের বাটনে চাপ দিন\nঅথবা স্ক্রিন নিচের দিকে টানুন",
                fontSize = 14.sp,
                color = Color(0xFFB0B0B0),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37),
                    contentColor = Color(0xFF0B3D2E),
                ),
            ) {
                Text(
                    text = "আজান বন্ধ করুন",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "আজান: Wikimedia Commons, CC BY-SA 4.0",
                fontSize = 11.sp,
                color = Color(0xFF808080),
                textAlign = TextAlign.Center,
            )
        }
    }
}
