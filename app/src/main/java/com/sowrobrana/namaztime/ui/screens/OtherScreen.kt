package com.sowrobrana.namaztime.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val DEV_NAME = "Sowrob Hasan Rana"
private const val DEV_EMAIL = "sowrobhasanrana39@gmail.com"

/**
 * অন্যান্য tab: app description, developer info, contact, share.
 * No login here — login arrives later with premium mode. Rating is
 * shown as coming soon until the app is published on Play Store.
 */
@Composable
fun OtherScreen() {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val version = try {
        context.packageManager
            .getPackageInfo(context.packageName, 0).versionName
    } catch (_: Exception) {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
    ) {
        Text(
            text = "অন্যান্য",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 12.dp),
        )

        // অ্যাপ সম্পর্কে
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Text(
                        text = "  অ্যাপ সম্পর্কে",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Namaz Time একটি সম্পূর্ণ অফলাইন নামাজের " +
                        "অ্যাপ। ইন্টারনেট ছাড়াই বাংলাদেশের ৬৪ জেলার " +
                        "সঠিক নামাজের সময়, হিজরি ক্যালেন্ডার, কিবলা " +
                        "কম্পাস, তাসবিহ, নামাজ শিক্ষা, দোয়া ও সম্পূর্ণ " +
                        "কুরআন (অর্থসহ ও মুসহাফ মোড) — সব এক জায়গায়। " +
                        "কোনো অ্যাকাউন্ট বা লগইন লাগে না।",
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ডেভেলপার
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                    Text(
                        text = "  ডেভেলপার",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = DEV_NAME, fontSize = 16.sp)
                Text(
                    text = "Namaz Time-এর ডেভেলপার",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // যোগাযোগ (ইমেইল)
        OtherRow(
            icon = Icons.Filled.Email,
            title = "যোগাযোগ",
            subtitle = DEV_EMAIL,
            onClick = {
                val intent = Intent(
                    Intent.ACTION_SENDTO,
                    Uri.parse("mailto:$DEV_EMAIL"),
                )
                context.startActivity(intent)
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // অ্যাপ শেয়ার করুন
        OtherRow(
            icon = Icons.Filled.Share,
            title = "অ্যাপ শেয়ার করুন",
            subtitle = "বন্ধুদের সাথে Namaz Time শেয়ার করুন",
            onClick = {
                val text = "Namaz Time — অফলাইন নামাজের সময়, " +
                    "কুরআন, দোয়া ও কিবলার অ্যাপ। " +
                    "শীঘ্রই Play Store-এ আসছে।"
                val intent = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    },
                    "অ্যাপ শেয়ার করুন",
                )
                context.startActivity(intent)
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // রেটিং দিন — Play Store প্রকাশের পর চালু হবে।
        OtherRow(
            icon = Icons.Filled.Star,
            title = "রেটিং দিন",
            subtitle = "শীঘ্রই আসছে — Play Store-এ প্রকাশের পর " +
                "এখান থেকে রেটিং দেওয়া যাবে।",
            enabled = false,
            onClick = {},
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (version != null) "সংস্করণ $version" else "",
            fontSize = 13.sp,
            color = Color.Gray,
        )
    }
}

@Composable
private fun OtherRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (enabled) it.clickable { onClick() } else it },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) Color.Unspecified else Color.Gray,
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.Unspecified else Color.Gray,
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}
