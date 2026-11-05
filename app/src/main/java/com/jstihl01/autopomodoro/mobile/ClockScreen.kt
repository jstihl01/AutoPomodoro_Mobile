package com.jstihl01.autopomodoro.mobile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

object AppDesign {
    val Work25 = Color(0xFF24985B)
    val Work50 = Color(0xFFC6384E)
    val Rest = Color(0xFF1E6BD3)
    val Clock = Color.Black
    val Settings = Color(0xFF174B80)
    val Button = Color(0xFF245FA2)
    val VolumeTrack = Color(0xFF55789F)
    val ButtonShape = RoundedCornerShape(14.dp)

    fun background(snapshot: DisplaySnapshot): Color = when {
        snapshot.mode == DisplayMode.CLOCK -> Clock
        snapshot.phase == Phase.REST -> Rest
        snapshot.mode == DisplayMode.EXTENDED -> Work50
        else -> Work25
    }
}

@Composable
fun AutoPomodoroTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Color.White, onPrimary = AppDesign.Settings,
        surface = AppDesign.Settings, onSurface = Color.White, background = Color.Black,
        secondary = Color.White, outline = Color.White.copy(alpha = 0.55f), surfaceTint = Color.Transparent),
        content = content)
}

@Composable
fun ClockScreen(snapshot: DisplaySnapshot, onSettings: () -> Unit) {
    Column(Modifier.fillMaxSize().background(AppDesign.background(snapshot))
        .windowInsetsPadding(WindowInsets.displayCutout).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().height(64.dp), horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSettings, modifier = Modifier.size(48.dp)
                .semantics { contentDescription = "Ajustes" }) {
                Icon(painterResource(R.drawable.ic_settings), contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
        AutoFitText(snapshot.text, Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp)
            .testTag("clock-text"), numeric = true)
        snapshot.phase?.let { phase ->
            AutoFitText(phase.label, Modifier.fillMaxWidth().weight(0.35f).padding(horizontal = 16.dp)
                .testTag("phase-text"), numeric = false, maxSp = 56f)
        }
        Spacer(Modifier.height(if (snapshot.mode == DisplayMode.CLOCK) 64.dp else 24.dp))
    }
}

@Composable
private fun AutoFitText(text: String, modifier: Modifier, numeric: Boolean, maxSp: Float = 400f) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = TextStyle(fontFamily = if (numeric) FontFamily.Monospace else FontFamily.SansSerif,
        fontWeight = if (numeric) FontWeight.Normal else FontWeight.SemiBold,
        fontFeatureSettings = "tnum", color = Color.White)
    // Measure a fixed template, so both the glyph widths and the font size stay stable as seconds change.
    val template = if (numeric) {
        if (text.length == 8) "88:88:88" else "88:88"
    } else "DESCANSO"
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }
        val fontSize = remember(template, width, height, density.density, density.fontScale, maxSp, numeric) {
            var low = 1f
            var high = maxSp
            repeat(12) {
                val middle = (low + high) / 2
                val layout = measurer.measure(template, style.copy(fontSize = middle.sp), softWrap = false)
                if (layout.size.width <= width && layout.size.height <= height) low = middle else high = middle
            }
            low
        }
        Text(text, style = style.copy(fontSize = fontSize.sp), maxLines = 1, softWrap = false)
    }
}

@Composable
private fun ModeCard(mode: DisplayMode, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = when (mode) {
        DisplayMode.STANDARD -> AppDesign.Work25
        DisplayMode.EXTENDED -> AppDesign.Work50
        DisplayMode.CLOCK -> AppDesign.Clock
    }
    Surface(modifier.selectable(selected, onClick = onClick, role = Role.RadioButton)
        .testTag("mode-${mode.name}"), color = color, shape = AppDesign.ButtonShape,
        border = BorderStroke(2.5.dp, if (selected) Color.White else Color.Transparent),
        tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Box(Modifier.heightIn(min = 58.dp).padding(12.dp), contentAlignment = Alignment.Center) {
            Text(when (mode) {
                DisplayMode.STANDARD -> "25 / 5"
                DisplayMode.EXTENDED -> "50 / 10"
                DisplayMode.CLOCK -> mode.label
            }, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumeSlider(volume: Float, onChange: (Float) -> Unit, onFinished: () -> Unit, modifier: Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Slider(value = volume, onValueChange = onChange, valueRange = 0f..100f,
        onValueChangeFinished = onFinished, interactionSource = interaction,
        modifier = modifier.heightIn(min = 48.dp).testTag("volume-slider")
            .semantics { contentDescription = "Volumen de los avisos" },
        thumb = {
            Box(Modifier.size(22.dp).background(if (focused) Color(0xFFA5D5FF) else Color.White, CircleShape))
        },
        track = { state ->
            Canvas(Modifier.fillMaxWidth().height(6.dp)) {
                val start = if (rtl) size.width else 0f
                val end = if (rtl) 0f else size.width
                drawLine(AppDesign.VolumeTrack, Offset(start, center.y), Offset(end, center.y),
                    strokeWidth = size.height, cap = StrokeCap.Round)
                val fraction = ((state.value - state.valueRange.start) /
                    (state.valueRange.endInclusive - state.valueRange.start)).coerceIn(0f, 1f)
                if (fraction > 0f) drawLine(Color.White, Offset(start, center.y),
                    Offset(start + (end - start) * fraction, center.y), strokeWidth = size.height, cap = StrokeCap.Round)
            }
        })
}

@Composable
fun SettingsDialog(settings: AppSettings, onMode: (DisplayMode) -> Unit, onVolume: (Int) -> Unit,
    onTestSound: () -> Unit, onReset: () -> Unit, onDismiss: () -> Unit) {
    var volume by remember(settings.volumePercent) { mutableFloatStateOf(settings.volumePercent.toFloat()) }
    var resetConfirmation by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.widthIn(max = 480.dp).fillMaxWidth(0.94f).padding(vertical = 16.dp),
            color = AppDesign.Settings, shape = RoundedCornerShape(20.dp), tonalElevation = 0.dp, shadowElevation = 0.dp) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AJUSTES", fontWeight = FontWeight.SemiBold, fontSize = 26.sp)
                Spacer(Modifier.height(4.dp))
                Text("VOLUMEN", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VolumeSlider(volume, { volume = it }, { onVolume(volume.roundToInt()) }, Modifier.weight(1f))
                    Text("${volume.roundToInt()} %", fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.widthIn(min = 64.dp), fontSize = 16.sp)
                }
                Button(onClick = onTestSound, enabled = settings.mode != DisplayMode.CLOCK && settings.volumePercent > 0,
                    shape = AppDesign.ButtonShape, elevation = null,
                    colors = ButtonDefaults.buttonColors(containerColor = AppDesign.Button, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    val icon = when (volume.roundToInt()) {
                        0 -> R.drawable.ic_volume_muted
                        in 1..33 -> R.drawable.ic_volume_low
                        in 34..66 -> R.drawable.ic_volume_medium
                        else -> R.drawable.ic_volume_high
                    }
                    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Probar volumen", fontWeight = FontWeight.SemiBold)
                }
                Text("MODO", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModeCard(DisplayMode.STANDARD, settings.mode == DisplayMode.STANDARD,
                            { onMode(DisplayMode.STANDARD) }, Modifier.weight(1f))
                        ModeCard(DisplayMode.EXTENDED, settings.mode == DisplayMode.EXTENDED,
                            { onMode(DisplayMode.EXTENDED) }, Modifier.weight(1f))
                    }
                    ModeCard(DisplayMode.CLOCK, settings.mode == DisplayMode.CLOCK,
                        { onMode(DisplayMode.CLOCK) }, Modifier.fillMaxWidth())
                }
                Text(if (settings.mode == DisplayMode.CLOCK) "El modo Reloj no emite sonidos."
                    else "Los avisos suenan sólo con la aplicación abierta. Usa también el volumen multimedia del teléfono.",
                    style = MaterialTheme.typography.bodySmall)
                Button(onClick = { resetConfirmation = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = AppDesign.ButtonShape, elevation = null,
                    colors = ButtonDefaults.buttonColors(containerColor = AppDesign.Button, contentColor = Color.White)) {
                    Text("RESTABLECER AJUSTES", fontWeight = FontWeight.SemiBold)
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = AppDesign.ButtonShape, elevation = null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppDesign.Settings)) {
                    Text("LISTO", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
    if (resetConfirmation) AlertDialog(onDismissRequest = { resetConfirmation = false },
        containerColor = AppDesign.Settings, tonalElevation = 0.dp, titleContentColor = Color.White,
        textContentColor = Color.White, shape = RoundedCornerShape(20.dp),
        title = { Text("¿Restablecer ajustes?") },
        text = { Text("Se seleccionará 25/5 y el volumen volverá al 100 %.") },
        confirmButton = { TextButton(onClick = { onReset(); resetConfirmation = false }) { Text("Restablecer") } },
        dismissButton = { TextButton(onClick = { resetConfirmation = false }) { Text("Cancelar") } })
}
