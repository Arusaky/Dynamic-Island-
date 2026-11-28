package com.akira.island.ui.setup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.runtime.Composable
import com.akira.island.core.tr
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.ui.Group
import com.akira.island.ui.GroupSlider
import com.akira.island.ui.Pos
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.screens.ChoiceStrip
import com.akira.island.ui.screens.Swatches
import kotlin.math.roundToInt

@Composable
internal fun MaterialPicker(s: IslandSettings) {
    SectionHeader(tr("Материал острова", "Island material"))
    ChoiceStrip(listOf(false, true), s.liquidGlass, { glass ->
        if (glass) tr("Стекло", "Glass") else tr("Обычный", "Solid")
    }) { SettingsRepo.update { copy(liquidGlass = it) } }
}

@Composable
internal fun CapsuleSizeControls(s: IslandSettings) {
    Group {
        GroupSlider(Pos.First, Icons.Rounded.Straighten, tr("Ширина капсулы", "Capsule width"),
            s.width.coerceIn(50, 400).toFloat(), 50f..400f, "${s.width} dp", color = Tile.Purple) { value ->
            SettingsRepo.update { copy(width = value.roundToInt()) }
        }
        GroupSlider(Pos.Last, Icons.Rounded.Height, tr("Высота капсулы", "Capsule height"),
            s.height.coerceIn(18, 90).toFloat(), 18f..90f, "${s.height} dp", color = Tile.Pink) { value ->
            SettingsRepo.update { copy(height = value.roundToInt()) }
        }
    }
}

@Composable
internal fun SetupStyleDetails(s: IslandSettings) {
    SectionHeader(tr("Форма", "Shape"))
    ChoiceStrip(listOf(true, false), s.cornerRadius < 0, { round ->
        if (round) tr("Капсула", "Capsule") else tr("Скруглённая", "Rounded rectangle")
    }) { round -> SettingsRepo.update { copy(cornerRadius = if (round) -1 else (height / 3).coerceAtLeast(4)) } }
    SectionHeader(tr("Цвет капсулы", "Capsule color"))
    Swatches(listOf(0xFF000000, 0xFF302451, 0xFF163B35, 0xFF4A3020), s.color) { color ->
        SettingsRepo.update { copy(color = color) }
    }
}
