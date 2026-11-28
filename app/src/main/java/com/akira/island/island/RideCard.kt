package com.akira.island.island

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.Controls
import com.akira.island.core.LiveInfo
import com.akira.island.core.Tints
import com.akira.island.core.tr

@Composable
internal fun RideCard(ride: LiveInfo) {
    val context = LocalContext.current
    val tint = Tints.Yellow
    val preview = LocalIslandPreview.current
    val detail = listOf(ride.text, ride.sub).firstOrNull { it.isNotBlank() && it != ride.title && it != ride.eta }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp).background(tint.copy(alpha = 0.17f), CircleShape), contentAlignment = Alignment.Center) {
                LiveGlyph(ride, 27.dp, tint)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(ride.title, color = Color.White, fontSize = 18.sp, lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold.island, maxLines = 2, overflow = TextOverflow.Ellipsis)
                ride.eta?.let { status ->
                    Text(status, color = tint, fontSize = 14.sp, style = Digits, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (detail != null) Text(detail, color = Tints.Gray, fontSize = 14.sp, lineHeight = 18.sp,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Row(
            Modifier.clip(CircleShape).background(tint)
                .clickable(enabled = !preview, role = Role.Button) {
                    if (!Controls.send(context, ride.open)) Controls.openApp(context, ride.pkg)
                    Controls.collapse()
                }
                .heightIn(min = 38.dp).padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(tr("Открыть поездку", "Open ride"), color = Color.Black, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold.island, modifier = Modifier.weight(1f, fill = false))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.Black, modifier = Modifier.size(15.dp))
        }
    }
}
