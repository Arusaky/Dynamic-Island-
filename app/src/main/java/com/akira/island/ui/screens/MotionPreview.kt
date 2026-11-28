package com.akira.island.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.island.PreviewScene
import com.akira.island.ui.SectionHeader

@Composable
internal fun MotionPreview() {
    val scenes = listOf(PreviewScene.MUSIC, PreviewScene.RECORDING, PreviewScene.TAXI, PreviewScene.CHARGING)
    var scene by rememberSaveable { mutableStateOf(PreviewScene.MUSIC) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    SectionHeader(tr("Активности и переходы", "Activities and transitions"))
    SettingsPreview(card = expanded, scene = scene, onExpand = { expanded = !expanded }, onSwipe = { direction ->
        scene = scenes[Math.floorMod(scenes.indexOf(scene) + direction, scenes.size)]
    })
    SettingsHint(tr("Смахните пример влево или вправо, чтобы сменить активность.",
        "Swipe the preview left or right to change the activity."))
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        scenes.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { candidate ->
                    FilterChip(selected = scene == candidate, onClick = { scene = candidate }, label = {
                        Text(when (candidate) {
                            PreviewScene.MUSIC -> tr("Музыка", "Music")
                            PreviewScene.RECORDING -> tr("Запись", "Recording")
                            PreviewScene.TAXI -> tr("Такси", "Taxi")
                            else -> tr("Зарядка", "Charging")
                        })
                    }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
    if (scene == PreviewScene.TAXI) CompactTaxiPicker()
}

@Composable
internal fun CompactTaxiPicker() {
    val s by SettingsRepo.state.collectAsState()
    SectionHeader(tr("Компактное такси", "Compact taxi"))
    ChoiceStrip(listOf(false, true), s.taxiEtaInCapsule, { eta ->
        if (eta) tr("Значок и время", "Icon and ETA") else tr("Только значок", "Icon only")
    }) { eta -> SettingsRepo.update { copy(taxiEtaInCapsule = eta) } }
}
