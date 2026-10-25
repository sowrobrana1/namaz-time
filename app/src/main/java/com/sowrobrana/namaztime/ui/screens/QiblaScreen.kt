package com.sowrobrana.namaztime.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sowrobrana.namaztime.prayer.Qibla
import com.sowrobrana.namaztime.ui.NamazViewModel
import com.sowrobrana.namaztime.util.bnNumber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Live compass: the dial stays fixed to the world, the top arrow is the phone's facing. */
@Composable
fun QiblaScreen(vm: NamazViewModel) {
    val city by vm.effectiveCity.collectAsState()
    val qiblaDeg = Qibla.bearing(city.lat, city.lng)
    val context = LocalContext.current

    var azimuth by remember { mutableStateOf<Float?>(null) }
    var hasSensors by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (acc == null || mag == null) {
            hasSensors = false
            onDispose { }
        } else {
            val gravity = FloatArray(3)
            val geomag = FloatArray(3)
            var hasGravity = false
            var hasMag = false
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        event.values.copyInto(gravity)
                        hasGravity = true
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        event.values.copyInto(geomag)
                        hasMag = true
                    }
                    if (!hasGravity || !hasMag) return
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomag)) {
                        val o = FloatArray(3)
                        SensorManager.getOrientation(r, o)
                        var deg = Math.toDegrees(o[0].toDouble()).toFloat()
                        if (deg < 0) deg += 360f
                        azimuth = deg
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sm.registerListener(listener, acc, SensorManager.SENSOR_DELAY_UI)
            sm.registerListener(listener, mag, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(listener) }
        }
    }

    val az = azimuth ?: 0f
    val diff = ((qiblaDeg - az) % 360 + 360) % 360
    val aligned = diff < 6 || diff > 354
    val turnText = when {
        !hasSensors -> "এই ফোনে কম্পাস সেন্সর পাওয়া যায়নি"
        azimuth == null -> "কম্পাস চালু হচ্ছে… ফোনটা একটু নাড়ান"
        aligned -> "আপনি কিবলার দিকে মুখ করে আছেন"
        diff < 180 -> "ডানদিকে ${bnNumber(diff.toInt())}° ঘুরুন"
        else -> "বামদিকে ${bnNumber((360 - diff).toInt())}° ঘুরুন"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "কিবলা কম্পাস",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "${city.name} থেকে কিবলা ${bnNumber(qiblaDeg.toInt())}°",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.size(300.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val radius = size.minDimension / 2 - 8.dp.toPx()
                drawCircle(
                    color = Color(0xFF0B3D2E),
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx()),
                )
                drawCircle(
                    color = Color(0xFFD9EDE4),
                    radius = radius - 3.dp.toPx(),
                )
                // Dial rotates opposite to the heading so it stays world-fixed.
                rotate(degrees = -az) {
                    for (deg in 0 until 360 step 15) {
                        val rad = deg * PI / 180
                        val major = deg % 90 == 0
                        val r1 = radius - if (major) 22.dp.toPx() else 14.dp.toPx()
                        val r2 = radius - 6.dp.toPx()
                        drawLine(
                            color = if (major) Color(0xFF0B3D2E) else Color(0xFF8AA89B),
                            start = Offset(
                                (cx + r1 * sin(rad)).toFloat(),
                                (cy - r1 * cos(rad)).toFloat(),
                            ),
                            end = Offset(
                                (cx + r2 * sin(rad)).toFloat(),
                                (cy - r2 * cos(rad)).toFloat(),
                            ),
                            strokeWidth = if (major) 3.dp.toPx() else 2.dp.toPx(),
                        )
                    }
                    // Cardinal letters, drawn upright-ish on the dial.
                    val labels = listOf("উ" to 0, "পূ" to 90, "দ" to 180, "প" to 270)
                    for ((letter, deg) in labels) {
                        val rad = deg * PI / 180
                        val r = radius - 40.dp.toPx()
                        drawContext.canvas.nativeCanvas.drawText(
                            letter,
                            (cx + r * sin(rad)).toFloat(),
                            (cy - r * cos(rad)).toFloat() + 14.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(0x0B, 0x3D, 0x2E)
                                textSize = 44f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            },
                        )
                    }
                    // Kaaba marker at the qibla bearing.
                    val qRad = qiblaDeg * PI / 180
                    val qr = radius - 62.dp.toPx()
                    drawCircle(
                        color = Color(0xFFC9962E),
                        radius = 20.dp.toPx(),
                        center = Offset(
                            (cx + qr * sin(qRad)).toFloat(),
                            (cy - qr * cos(qRad)).toFloat(),
                        ),
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 9.dp.toPx(),
                        center = Offset(
                            (cx + qr * sin(qRad)).toFloat(),
                            (cy - qr * cos(qRad)).toFloat(),
                        ),
                    )
                }
                // Fixed top arrow = phone's facing direction.
                val arrow = Path().apply {
                    moveTo(cx, 2.dp.toPx())
                    lineTo(cx - 14.dp.toPx(), 26.dp.toPx())
                    lineTo(cx + 14.dp.toPx(), 26.dp.toPx())
                    close()
                }
                drawPath(arrow, color = Color(0xFFC9962E))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = turnText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (aligned) Color(0xFF0E7A5B) else MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "সোনালি চিহ্নটা উপরের তীরের সাথে মিললে আপনি কিবলামুখী।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "কম্পাসের নির্ভুলতার জন্য ফোনটা ৮-এর মতো করে দু-একবার নাড়াতে পারেন।",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
