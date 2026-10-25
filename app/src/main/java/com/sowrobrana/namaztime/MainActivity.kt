package com.sowrobrana.namaztime

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sowrobrana.namaztime.alarms.PrayerAlarmScheduler
import com.sowrobrana.namaztime.data.Prefs
import com.sowrobrana.namaztime.prayer.BD_CITIES
import com.sowrobrana.namaztime.prayer.PrayerTimes
import com.sowrobrana.namaztime.ui.NamazViewModel
import com.sowrobrana.namaztime.ui.screens.CalendarScreen
import com.sowrobrana.namaztime.ui.screens.QiblaScreen
import com.sowrobrana.namaztime.ui.screens.TasbihScreen
import com.sowrobrana.namaztime.ui.screens.TimesScreen
import com.sowrobrana.namaztime.ui.theme.NamazTheme

class MainActivity : ComponentActivity() {

    private val vm: NamazViewModel by viewModels()

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* banner in TimesScreen covers the exact-alarm part */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (prefs.notifyEnabled) {
            val city = BD_CITIES.getOrElse(prefs.cityIndex) { BD_CITIES[0] }
            PrayerAlarmScheduler.scheduleAll(
                this, city, PrayerTimes.factorForMazhab(prefs.mazhab),
            )
        }
        setContent {
            NamazTheme {
                MainNav(vm = vm, onRequestExactAlarm = { openExactAlarmSettings() })
            }
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }
}

private data class Tab(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
private fun MainNav(vm: NamazViewModel, onRequestExactAlarm: () -> Unit) {
    val nav = rememberNavController()
    val tabs = listOf(
        Tab("somoy", "সময়") { Icon(Icons.Filled.AccessTime, contentDescription = null) },
        Tab("calendar", "ক্যালেন্ডার") { Icon(Icons.Filled.DateRange, contentDescription = null) },
        Tab("qibla", "কিবলা") { Icon(Icons.Filled.Explore, contentDescription = null) },
        Tab("tasbih", "তাসবিহ") { Icon(Icons.Filled.RadioButtonChecked, contentDescription = null) },
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by nav.currentBackStackEntryAsState()
                val current = backStack?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = tab.icon,
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = "somoy",
            modifier = Modifier.padding(inner),
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut() },
        ) {
            composable("somoy") { TimesScreen(vm, onRequestExactAlarm) }
            composable("calendar") { CalendarScreen() }
            composable("qibla") { QiblaScreen(vm) }
            composable("tasbih") { TasbihScreen(vm) }
        }
    }
}
