package com.akira.island.ui.setup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.runtime.Composable
import com.akira.island.core.tr
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.ui.Group
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Pos
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile

@Composable
internal fun SetupActivities(s: IslandSettings) {
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
