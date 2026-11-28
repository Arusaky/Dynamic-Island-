package com.akira.island.ui.setup

import android.os.Build

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.tr
import com.akira.island.core.trf
import com.akira.island.data.SettingsRepo
import com.akira.island.island.PreviewScene
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.Perms
import com.akira.island.ui.Pos
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui
import com.akira.island.ui.screens.SettingsPreview
import com.akira.island.ui.screens.rememberOnResume
import com.akira.island.ui.screens.restrictedHelp

private enum class SetupStep { ACCESS, STYLE, ACTIVITIES, READY }

internal data class SetupAccess(val accessibility: Boolean, val notifications: Boolean, val battery: Boolean) {
    val ready: Boolean get() = accessibility && notifications
}

private val SetupAccent = Color(0xFF8DE6CB)
private val SetupBackground = Color(0xFF121219)

@Composable
internal fun SetupScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val (access, _) = rememberOnResume {
        SetupAccess(Perms.accessibility(context), Perms.notifications(context), Perms.batteryUnrestricted(context))
    }
    SetupFlow(access, { Perms.openAccessibility(context) }, { Perms.openNotifications(context) },
        { Perms.openBattery(context) }, { Perms.openAppDetails(context) }, onFinish)
}

@Composable
internal fun SetupFlow(
    access: SetupAccess,
    openAccessibility: () -> Unit,
    openNotifications: () -> Unit,
    openBattery: () -> Unit,
    openAppDetails: () -> Unit,
    onFinish: () -> Unit,
) {
    val settings by SettingsRepo.state.collectAsState()
    var requestedStep by rememberSaveable { mutableStateOf(SetupStep.ACCESS) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var scene by rememberSaveable { mutableStateOf(PreviewScene.MUSIC) }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(access.ready) { if (!access.ready) requestedStep = SetupStep.ACCESS }
    val step = if (access.ready) requestedStep else SetupStep.ACCESS
    val back = { requestedStep = SetupStep.entries[step.ordinal - 1] }
    BackHandler(enabled = step != SetupStep.ACCESS, onBack = back)
    if (showHelp && step == SetupStep.ACCESS) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(tr("Ограниченные настройки", "Restricted settings")) },
            text = { Text(restrictedHelp()) },
            confirmButton = {
                TextButton(onClick = { showHelp = false; openAppDetails() }) {
                    Text(tr("Открыть настройки приложения", "Open app settings"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showHelp = false }) { Text(tr("Закрыть", "Close")) }
            },
        )
    }

    Box(Modifier.fillMaxSize().background(SetupBackground)) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(Brush.radialGradient(listOf(Color(0xFF272340), Color.Transparent),
                center = Offset(size.width * 0.94f, 0f), radius = size.width * 0.95f))
            drawRect(Brush.radialGradient(listOf(Color(0xFF183C36), Color.Transparent),
                center = Offset(size.width * 0.09f, size.height), radius = size.width * 0.96f))
        }
        Column(
            Modifier.fillMaxHeight().fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 22.dp),
        ) {
            Row(Modifier.fillMaxWidth().heightIn(min = 54.dp), verticalAlignment = Alignment.CenterVertically) {
                if (step != SetupStep.ACCESS) {
                    IconButton(onClick = back) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, tr("Назад", "Back"), tint = Ui.Text)
                    }
                }
                Text(
                    if (step == SetupStep.READY) "Dynamic Island" else trf("Шаг {0} из 3", "Step {0} of 3", step.ordinal + 1),
                    color = Ui.Sub, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { index ->
                    val tint by animateColorAsState(if (index <= step.ordinal) SetupAccent else Ui.IconBg,
                        tween(240), label = "setup progress")
                    Box(Modifier.weight(1f).height(4.dp).background(tint, CircleShape))
                }
            }
            AnimatedContent(
                targetState = step,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (fadeIn(tween(220)) + slideInHorizontally(tween(240)) { direction * it / 10 }) togetherWith
                        (fadeOut(tween(130)) + slideOutHorizontally(tween(190)) { -direction * it / 12 })
                },
                label = "setup screen",
            ) { current ->
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 22.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (current) {
                        SetupStep.ACCESS -> {
                            SetupEmblem(Icons.Rounded.Notifications)
                            SetupTitle(tr("Разрешения", "Permissions"),
                                tr("Разрешите показывать остров поверх приложений и получать события из уведомлений.",
                                    "Allow the island to appear over apps and receive events from notifications."))
                            Group {
                                PermissionRow(Pos.First, Icons.Rounded.AccessibilityNew,
                                    tr("Спец. возможности", "Accessibility"),
                                    tr("Показывает остров поверх приложений", "Shows the island over apps"),
                                    access.accessibility, openAccessibility)
                                PermissionRow(Pos.Middle, Icons.Rounded.Notifications,
                                    tr("Доступ к уведомлениям", "Notification access"),
                                    tr("Музыка, сообщения и поездки", "Music, messages and rides"),
                                    access.notifications, openNotifications)
                                PermissionRow(Pos.Last, Icons.Rounded.BatterySaver,
                                    tr("Работа в фоне", "Background use"),
                                    tr("Необязательно · помогает, если система закрывает остров",
                                        "Optional · helps if Android closes the island"),
                                    access.battery, openBattery)
                            }
                            if (!access.ready && Build.VERSION.SDK_INT >= 33) TextButton(onClick = { showHelp = true }) {
                                Text(tr("Не удаётся включить разрешение?", "Can't grant access?"), color = SetupAccent)
                            }
                        }
                        SetupStep.STYLE -> {
                            SetupTitle(tr("Ваш остров", "Your island"),
                                tr("Пример меняется вместе с оформлением.", "The preview updates as you customize the island."))
                            SettingsPreview(card = expanded, scene = scene, onScene = { scene = it },
                                onExpand = { expanded = !expanded })
                            MaterialPicker(settings)
                            CapsuleSizeControls(settings)
                            SetupStyleDetails(settings)
                        }
                        SetupStep.ACTIVITIES -> {
                            SetupEmblem(Icons.Rounded.Widgets)
                            SetupTitle(tr("Ваши активности", "Your activities"),
                                tr("Выберите, какие события показывать в острове.", "Choose what appears in your island."))
                            SetupActivities(settings)
                        }
                        SetupStep.READY -> {
                            Box(Modifier.padding(top = 48.dp)) { SetupEmblem(Icons.Rounded.Check) }
                            SetupTitle(tr("Остров готов!", "Your island is ready!"),
                                tr("Включите музыку или дождитесь уведомления, чтобы увидеть остров в деле.",
                                    "Play music or wait for a notification to see the island in action."))
                        }
                    }
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp).heightIn(min = 54.dp)
                    .testTag("setup-next"),
                onClick = {
                    if (access.ready) {
                        if (step == SetupStep.READY) onFinish()
                        else requestedStep = SetupStep.entries[step.ordinal + 1]
                    }
                },
                enabled = access.ready,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SetupAccent, contentColor = Color(0xFF13251F),
                    disabledContainerColor = Color(0xFF303737), disabledContentColor = Color(0xFFB3BCB8)),
            ) {
                Text(if (step == SetupStep.READY) tr("На главную", "Go to Home") else tr("Далее", "Next"),
                    fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SetupTitle(title: String, text: String) {
    Text(title, color = Ui.Text, fontSize = 30.sp, lineHeight = 34.sp,
        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Text(text, color = Ui.Sub, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
}

@Composable
private fun SetupEmblem(icon: ImageVector) {
    Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Brush.radialGradient(listOf(SetupAccent.copy(alpha = 0.28f), Color.Transparent)))
            drawCircle(Color(0xFF243631), radius = size.minDimension * 0.37f)
        }
        Icon(icon, contentDescription = null, tint = SetupAccent, modifier = Modifier.size(38.dp))
    }
}

@Composable
private fun PermissionRow(
    position: Pos,
    icon: ImageVector,
    title: String,
    subtitle: String,
    granted: Boolean,
    open: () -> Unit,
) {
    val status = if (granted) tr("Разрешено", "Granted") else tr("Открыть настройки", "Open settings")
    GroupItem(position, icon, title, subtitle, color = Tile.Teal, onClick = open) {
        Box(Modifier.size(28.dp).background(if (granted) SetupAccent.copy(alpha = 0.16f) else Ui.IconBg,
            CircleShape).semantics { contentDescription = status }, contentAlignment = Alignment.Center) {
            if (granted) Icon(Icons.Rounded.Check, null, tint = SetupAccent, modifier = Modifier.size(17.dp))
            else Text("›", color = Ui.Sub, fontSize = 18.sp)
        }
    }
}
