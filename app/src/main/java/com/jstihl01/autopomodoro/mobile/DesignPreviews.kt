package com.jstihl01.autopomodoro.mobile

import android.widget.ImageView
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Preview(widthDp = 360, heightDp = 800)
@Composable
fun StandardDesignPreview() {
    AutoPomodoroTheme {
        ClockScreen(DisplaySnapshot(DisplayMode.STANDARD, "25:00", Phase.WORK, 1500), {})
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
fun ExtendedDesignPreview() {
    AutoPomodoroTheme {
        ClockScreen(DisplaySnapshot(DisplayMode.EXTENDED, "50:00", Phase.WORK, 3000), {})
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
fun RestDesignPreview() {
    AutoPomodoroTheme {
        ClockScreen(DisplaySnapshot(DisplayMode.STANDARD, "05:00", Phase.REST, 300), {})
    }
}

@Preview(widthDp = 800, heightDp = 360)
@Composable
fun ClockDesignPreview() {
    AutoPomodoroTheme {
        ClockScreen(DisplaySnapshot(DisplayMode.CLOCK, "23:59:58"), {})
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
fun SettingsDesignPreview() {
    AutoPomodoroTheme {
        SettingsDialog(AppSettings(), {}, {}, {}, {}, {})
    }
}

@Preview(widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
fun LargeSettingsDesignPreview() {
    AutoPomodoroTheme {
        SettingsDialog(AppSettings(), {}, {}, {}, {}, {})
    }
}

@Preview(widthDp = 180, heightDp = 180)
@Composable
fun LauncherIconDesignPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFFE8E8E8)).padding(20.dp)) {
        AndroidView(factory = { ImageView(it).apply { setImageResource(R.mipmap.ic_launcher) } },
            modifier = Modifier.fillMaxSize())
    }
}

@Preview(widthDp = 360, heightDp = 160)
@Composable
fun LauncherMasksDesignPreview() {
    Row(Modifier.fillMaxSize().background(Color(0xFFE8E8E8)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf("Circular", "Redondeado", "Cuadrado").forEachIndexed { index, label ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                AndroidView(factory = { LauncherMaskView(it, index).apply { setImageResource(R.mipmap.ic_launcher) } },
                    modifier = Modifier.size(96.dp))
                Text(label, color = Color.Black, fontSize = 12.sp)
            }
        }
    }
}

private class LauncherMaskView(context: Context, private val mask: Int) : ImageView(context) {
    override fun onDraw(canvas: Canvas) {
        val path = Path().apply {
            when (mask) {
                0 -> addCircle(width / 2f, height / 2f, minOf(width, height) / 2f, Path.Direction.CW)
                1 -> addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), width * 0.25f, height * 0.25f, Path.Direction.CW)
                else -> addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
            }
        }
        val save = canvas.save()
        canvas.clipPath(path)
        super.onDraw(canvas)
        canvas.restoreToCount(save)
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
fun ClockPortraitDesignPreview() {
    AutoPomodoroTheme { ClockScreen(DisplaySnapshot(DisplayMode.CLOCK, "23:59:58"), {}) }
}

@Preview(widthDp = 320, heightDp = 568)
@Composable
fun SmallScreenDesignPreview() {
    AutoPomodoroTheme { ClockScreen(DisplaySnapshot(DisplayMode.STANDARD, "11:11", Phase.WORK, 671), {}) }
}

@Preview(widthDp = 640, heightDp = 360)
@Composable
fun SettingsLandscapeDesignPreview() {
    AutoPomodoroTheme { SettingsDialog(AppSettings(volumePercent = 50), {}, {}, {}, {}, {}) }
}
