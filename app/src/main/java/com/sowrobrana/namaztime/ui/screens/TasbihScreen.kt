package com.sowrobrana.namaztime.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sowrobrana.namaztime.ui.NamazViewModel
import com.sowrobrana.namaztime.util.bnNumber

val DHIKRS = listOf(
    "সুবহানাল্লাহ",
    "আলহামদুলিল্লাহ",
    "আল্লাহু আকবর",
    "লা ইলাহা ইল্লাল্লাহ",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasbihScreen(vm: NamazViewModel) {
    val dhikrIndex by vm.tasbihDhikr.collectAsState()
    val target by vm.tasbihTarget.collectAsState()
    val count by vm.tasbihCount.collectAsState()
    val haptics = LocalHapticFeedback.current

    val inCycle = if (target > 0) count % target else 0
    val cycles = if (target > 0) count / target else 0
    val progress = if (target > 0) inCycle.toFloat() / target else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
        label = "tasbihRing",
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "tasbihPress",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "ডিজিটাল তাসবিহ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DHIKRS.forEachIndexed { i, name ->
                FilterChip(
                    selected = dhikrIndex == i,
                    onClick = { vm.selectDhikr(i) },
                    label = { Text(name, fontSize = 12.sp) },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "লক্ষ্য:", fontSize = 14.sp)
            listOf(33, 99, 100).forEach { t ->
                FilterChip(
                    selected = target == t,
                    onClick = { vm.setTarget(t) },
                    label = { Text(bnNumber(t), fontSize = 13.sp) },
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 14.dp.toPx()
                drawArc(
                    color = Color(0xFFD9EDE4),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = Color(0xFFE8B84B),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            Button(
                onClick = {
                    vm.tapTasbih()
                    val next = count + 1
                    if (target > 0 && next % target == 0) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
                interactionSource = interaction,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = DHIKRS[dhikrIndex],
                        color = Color(0xFFE8B84B),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = bnNumber(count),
                        color = Color.White,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "ট্যাপ করুন",
                        color = Color(0xFFD9EDE4),
                        fontSize = 13.sp,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "এই চক্রে: ${bnNumber(inCycle)} / ${bnNumber(target)}  •  সম্পন্ন চক্র: ${bnNumber(cycles)}",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { vm.resetTasbih() }) {
            Text("আবার শুরু করুন")
        }
    }
}
