package com.akira.island.island

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.Controls
import com.akira.island.core.LiveKind
import com.akira.island.core.tr

@Composable
internal fun ActivityShortcut(act: Act, modifier: Modifier = Modifier) {
    val label = activityLabel(act)
    val haptic = rememberHaptic()
    Row(
        modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.11f))
            .clickable(role = Role.Button, onClickLabel = tr("Переключить активность", "Switch activity")) {
                haptic()
                Controls.expand(act.id)
            }
            .heightIn(min = 38.dp).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium.island,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White.copy(alpha = 0.72f),
            modifier = Modifier.size(15.dp))
    }
}

private fun activityLabel(act: Act): String = when (act) {
    is Act.MediaA -> tr("Плеер", "Player")
    is Act.ChronoA -> act.c.recordingKind?.title ?: if (act.c.countDown) tr("Таймер", "Timer") else tr("Секундомер", "Stopwatch")
    is Act.ChargingA -> tr("Зарядка", "Charging")
    is Act.LiveA -> when (act.l.kind) {
        LiveKind.RIDE -> tr("Такси", "Taxi")
        LiveKind.NAV -> tr("Навигация", "Navigation")
        LiveKind.DELIVERY -> tr("Доставка", "Delivery")
    }
    is Act.AlarmA -> tr("Будильник", "Alarm")
    is Act.CallA -> tr("Звонок", "Call")
    is Act.TorchA -> tr("Фонарик", "Flashlight")
    is Act.TimerA -> tr("Таймер", "Timer")
    is Act.StopwatchA -> tr("Секундомер", "Stopwatch")
    is Act.ProgressA -> tr("Прогресс", "Progress")
    is Act.HotspotA -> tr("Точка доступа", "Hotspot")
    is Act.InboxA -> tr("Уведомления", "Notifications")
    else -> tr("Активность", "Activity")
}

/** Keep the card open only for a real replacement. Future alarms still require selection. */
internal fun replacementForExpandedFocus(acts: List<Act>): String? =
    acts.firstOrNull {
        it !is Act.Idle && it !is Act.AlarmA && !it.transient &&
            !(it is Act.CallA && it.c.incoming) && !(it is Act.TimerA && it.t.ringing)
    }?.id
