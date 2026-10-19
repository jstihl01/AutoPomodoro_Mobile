package com.jstihl01.autopomodoro.mobile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

@Composable
fun AutoPomodoroTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF9BE4B2),
        surface = Color(0xFF171A1C), background = Color.Black), content = content)
}

@Composable
fun ClockScreen(snapshot: DisplaySnapshot, onSettings: () -> Unit) {
    val colors = when {
        snapshot.mode == DisplayMode.CLOCK -> listOf(Color(0xFF17191D), Color.Black)
        snapshot.phase == Phase.REST -> listOf(Color(0xFF3083F6), Color(0xFF055BCA))
        snapshot.mode == DisplayMode.EXTENDED -> listOf(Color(0xFFE04848), Color(0xFFA11E30))
        else -> listOf(Color(0xFF63B447), Color(0xFF16804D))
    }
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(colors))) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout)
            .padding(horizontal = 24.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AutoFitText(snapshot.text, Modifier.fillMaxWidth().weight(1f).testTag("clock-text"))
            snapshot.phase?.let { phase ->
                AutoFitText(phase.label, Modifier.fillMaxWidth(0.8f).weight(0.27f).testTag("phase-text"), maxSp = 64f)
            }
        }
        IconButton(onClick = onSettings,
            modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.displayCutout)
                .padding(8.dp).size(48.dp).semantics { contentDescription = "Ajustes" }) {
            Canvas(Modifier.size(24.dp)) {
                for (fraction in listOf(0.2f, 0.5f, 0.8f)) {
                    drawLine(Color.White.copy(alpha = 0.8f), Offset(0f, size.height * fraction),
                        Offset(size.width, size.height * fraction), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun AutoFitText(text: String, modifier: Modifier, maxSp: Float = 400f) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }
        val baseStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold,
            fontFeatureSettings = "tnum", color = Color.White)
        val fontSize = remember(text, width, height, density.fontScale, maxSp) {
            var low = 1f
            var high = maxSp
            repeat(12) {
                val middle = (low + high) / 2
                val layout = measurer.measure(text, baseStyle.copy(fontSize = middle.sp), softWrap = false)
                if (layout.size.width <= width && layout.size.height <= height) low = middle else high = middle
            }
            low
        }
        Text(text, style = baseStyle.copy(fontSize = fontSize.sp), maxLines = 1, softWrap = false)
    }
}

@Composable
fun SettingsDialog(settings: AppSettings, onMode: (DisplayMode) -> Unit, onVolume: (Int) -> Unit,
    onTestSound: () -> Unit, onReset: () -> Unit, onDismiss: () -> Unit) {
    var volume by remember(settings.volumePercent) { mutableFloatStateOf(settings.volumePercent.toFloat()) }
    var resetConfirmation by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.widthIn(max = 480.dp).fillMaxWidth(0.94f).padding(vertical = 24.dp),
            shape = MaterialTheme.shapes.extraLarge) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
                Text("Modo", style = MaterialTheme.typography.titleSmall)
                DisplayMode.entries.forEach { mode ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .selectable(settings.mode == mode, onClick = { onMode(mode) }, role = Role.RadioButton)
                        .testTag("mode-${mode.name}"), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = settings.mode == mode, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(mode.label, style = MaterialTheme.typography.titleMedium)
                    }
                }
                HorizontalDivider()
                Text("Volumen: ${volume.roundToInt()} %")
                Slider(value = volume, onValueChange = { volume = it }, valueRange = 0f..100f,
                    onValueChangeFinished = { onVolume(volume.roundToInt()) },
                    modifier = Modifier.semantics { contentDescription = "Volumen de los avisos" })
                OutlinedButton(onClick = onTestSound, enabled = settings.mode != DisplayMode.CLOCK && settings.volumePercent > 0) {
                    Text("Probar sonido")
                }
                Text(if (settings.mode == DisplayMode.CLOCK) "El modo Reloj no emite sonidos."
                    else "Los avisos se reproducen sólo mientras la aplicación está abierta.",
                    style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { resetConfirmation = true }) { Text("Restablecer ajustes") }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("LISTO") }
            }
        }
    }
    if (resetConfirmation) AlertDialog(onDismissRequest = { resetConfirmation = false },
        title = { Text("¿Restablecer ajustes?") },
        text = { Text("Se seleccionará 25/5 y el volumen volverá al 60 %.") },
        confirmButton = { TextButton(onClick = { onReset(); resetConfirmation = false }) { Text("Restablecer") } },
        dismissButton = { TextButton(onClick = { resetConfirmation = false }) { Text("Cancelar") } })
}
