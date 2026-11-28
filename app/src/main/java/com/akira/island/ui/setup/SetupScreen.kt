package com.akira.island.ui.setup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.akira.island.core.tr
import com.akira.island.core.trf
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Perms
import com.akira.island.ui.PillAction
import com.akira.island.ui.Pos
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui
import com.akira.island.ui.screens.SettingsPreview
import com.akira.island.ui.screens.Status
import com.akira.island.ui.screens.rememberOnResume

@Composable
internal fun SetupScreen(close: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    var step by rememberSaveable { mutableIntStateOf(0) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val finish = {
        SettingsRepo.finishSetup()
        close()
    }
    val back: () -> Unit = {
        if (step > 0) step-- else finish()
    }
    BackHandler(onBack = back)
    val scroll = rememberScrollState()
    LaunchedEffect(step) { scroll.scrollTo(0) }
    Page(
        tr("Настройка острова", "Island setup"),
        onBack = back,
        scroll = scroll,
        action = { PillAction(tr("Пропустить", "Skip"), finish) },
    ) {
        Text(trf("Шаг {0} из 4", "Step {0} of 4", step + 1), color = Ui.Primary)
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn(tween(210)) togetherWith fadeOut(tween(110)) },
            label = "setup step",
        ) { current ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (current) {
                    0 -> {
                        SettingsPreview(card = expanded, onExpand = { expanded = !expanded })
                        MaterialPicker(s)
                        CapsuleSizeControls(s)
                        SetupStyleDetails(s)
                    }
                    1 -> SetupActivities(s)
                    2 -> SetupPermissions()
                    3 -> {
                        SettingsPreview(card = expanded, onExpand = { expanded = !expanded })
                        Text(tr("Настройки сохранены. Их можно изменить в разделах «Оформление» и «Функции».",
                            "Your settings are saved. You can change them in Appearance and Features."), color = Ui.Sub)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            PillAction(if (step == 3) tr("На главную", "Go to Home") else tr("Далее", "Next")) {
                if (step == 3) finish() else step++
            }
        }
    }
}

@Composable
private fun SetupActivities(s: IslandSettings) {
    Text(tr("Выберите, что видно в острове. Дополнительные функции можно включить здесь или позже.",
        "Choose what appears in the island. Turn optional features on here or later."), color = Ui.Sub)
    Group {
        GroupToggle(Pos.First, Icons.Rounded.MusicNote, tr("Плеер", "Music player"), null, s.cardMedia, color = Tile.Purple) {
            SettingsRepo.update { copy(cardMedia = it) }
        }
        GroupToggle(Pos.Middle, Icons.Rounded.Notifications, tr("Сообщения", "Messages"), null, s.cardMessages, color = Tile.Blue) {
            SettingsRepo.update { copy(cardMessages = it) }
        }
        GroupToggle(Pos.Middle, Icons.Rounded.Bolt, tr("Зарядка", "Charging"), null, s.cardCharging, color = Tile.Green) {
            SettingsRepo.update { copy(cardCharging = it) }
        }
        GroupToggle(Pos.Middle, Icons.Rounded.CameraAlt, tr("Запись", "Recording"),
            tr("Экран и диктофон", "Screen and voice recording"), s.cardRecording, color = Tile.Red) {
            SettingsRepo.update { copy(cardRecording = it) }
        }
        GroupToggle(Pos.Middle, Icons.Rounded.DirectionsCar, tr("Такси, навигация и доставка", "Taxi, navigation and delivery"),
            null, s.cardLive, color = Tile.Yellow) {
            SettingsRepo.update { copy(cardLive = it) }
        }
        GroupToggle(Pos.Last, Icons.Rounded.Swipe, tr("Листать активности", "Swipe between activities"),
            tr("Свайп влево или вправо переключает событие", "Swipe left or right to switch events"), s.swipeActivities, color = Tile.Teal) {
            SettingsRepo.update { copy(swipeActivities = it) }
        }
    }
    SectionHeader(tr("Дополнительно", "More options"))
    Group {
        GroupToggle(Pos.First, Icons.Rounded.Speed, tr("Скорость сети", "Network speed"),
            tr("В раскрытой карточке загрузки", "In the expanded download card"), s.showNetworkRate, color = Tile.Indigo) {
            SettingsRepo.update { copy(showNetworkRate = it) }
        }
        GroupToggle(Pos.Last, Icons.Rounded.WifiTethering, tr("Точка доступа", "Hotspot"),
            null, s.evHotspot, color = Tile.Teal) {
            SettingsRepo.update { copy(evHotspot = it) }
        }
    }
}

@Composable
private fun SetupPermissions() {
    val context = LocalContext.current
    val (access, _) = rememberOnResume { Perms.accessibility(context) to Perms.notifications(context) }
    Text(tr("Android попросит подтвердить доступ. Настройку можно закончить без выдачи разрешений.",
        "Android will ask you to confirm access. You can finish setup without granting permissions."), color = Ui.Sub)
    Group {
        GroupItem(Pos.First, Icons.Rounded.AccessibilityNew, tr("Спец. возможности", "Accessibility"),
            tr("Показывает остров над другими приложениями", "Shows the island over other apps"),
            color = Tile.Blue, onClick = { Perms.openAccessibility(context) }) { Status(access.first) }
        GroupItem(Pos.Last, Icons.Rounded.Notifications, tr("Доступ к уведомлениям", "Notification access"),
            tr("Для музыки, сообщений и поездок", "For music, messages and rides"),
            color = Tile.Purple, onClick = { Perms.openNotifications(context) }) { Status(access.second) }
    }
}
