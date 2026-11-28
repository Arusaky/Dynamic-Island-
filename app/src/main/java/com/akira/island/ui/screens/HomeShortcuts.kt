package com.akira.island.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.tr
import com.akira.island.ui.ItemIcon
import com.akira.island.ui.Screen
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui

@Composable
internal fun HomeShortcuts(open: (Screen) -> Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Shortcut(Icons.Rounded.Straighten, tr("Размер и форма", "Size and shape"), tr("Капсула и карточка", "Capsule and card"),
            Tile.Purple, Modifier.weight(1f).fillMaxHeight()) { open(Screen.LOOK) }
        Shortcut(Icons.Rounded.Widgets, tr("Активности", "Activities"), tr("Что показывать", "What to show"),
            Tile.Teal, Modifier.weight(1f).fillMaxHeight()) { open(Screen.CARDS) }
    }
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Shortcut(Icons.Rounded.Notifications, tr("Уведомления", "Notifications"), tr("Вид и приложения", "Style and apps"),
            Tile.Blue, Modifier.weight(1f).fillMaxHeight()) { open(Screen.NOTIFICATIONS) }
        Shortcut(Icons.Rounded.Animation, tr("Движение", "Motion"), tr("Попробовать переходы", "Try transitions"),
            Tile.Pink, Modifier.weight(1f).fillMaxHeight()) { open(Screen.TESTS) }
    }

}

@Composable
private fun Shortcut(icon: ImageVector, title: String, subtitle: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.clip(shape).background(Ui.Card).background(tint.copy(alpha = 0.08f))
            .border(BorderStroke(1.dp, tint.copy(alpha = 0.18f)), shape)
            .clickable(onClick = onClick).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        ItemIcon(icon, tint, 30.dp)
        Text(title, color = Ui.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
        Text(subtitle, color = Ui.Sub, fontSize = 12.sp, lineHeight = 15.sp)
    }
}
