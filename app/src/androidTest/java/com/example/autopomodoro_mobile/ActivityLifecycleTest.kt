package com.jstihl01.autopomodoro.mobile

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.WindowManager
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.Calendar
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class ActivityLifecycleTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun settings() { compose.onNodeWithContentDescription("Ajustes").performClick() }
    private fun select(mode: DisplayMode) {
        settings()
        compose.onNodeWithTag("mode-${mode.name}").performScrollTo().performClick()
        compose.waitUntil(10000) {
            compose.onAllNodesWithTag("mode-${mode.name}").fetchSemanticsNodes()
                .any { it.config.contains(SemanticsProperties.Selected) && it.config[SemanticsProperties.Selected] }
        }
        compose.onNodeWithText("LISTO").performScrollTo().performClick()
        compose.waitForIdle()
    }
    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "screenshots/$name.png")
        file.parentFile!!.mkdirs()
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun shell(command: String): String =
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
            .use { descriptor -> android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().readText() }

    @Test fun modesRotationRecreationAndForegroundFlag() {
        compose.waitUntil(15000) { compose.onAllNodesWithContentDescription("Ajustes").fetchSemanticsNodes().isNotEmpty() }
        select(DisplayMode.STANDARD)
        capture("25-5-portrait")
        select(DisplayMode.EXTENDED)
        capture("50-10-portrait")
        select(DisplayMode.CLOCK)
        capture("clock-portrait")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("phase-text").assertDoesNotExist()
        compose.activityRule.scenario.onActivity {
            assertTrue(it.window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0)
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        compose.waitForIdle()
        SystemClock.sleep(1500)
        capture("clock-landscape")
        shell("settings put system font_scale 1.5")
        SystemClock.sleep(1500)
        compose.onNodeWithTag("clock-text").assertIsDisplayed()
        capture("clock-landscape-large-text")
        shell("settings put system font_scale 1.0")
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.onActivity {
            assertEquals(0, it.window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.onNodeWithTag("clock-text").assertIsDisplayed()
        select(DisplayMode.STANDARD)
    }

    @Test fun clockKeepsScreenAwakeForOneHour() {
        val minutes = InstrumentationRegistry.getArguments().getString("soakMinutes")?.toIntOrNull() ?: 0
        assumeTrue("Run with soakMinutes=60 for the endurance test", minutes >= 60)
        compose.waitUntil(15000) { compose.onAllNodesWithContentDescription("Ajustes").fetchSemanticsNodes().isNotEmpty() }
        val oldTimeout = shell("settings get system screen_off_timeout").trim()
        val oldCharging = shell("settings get global stay_on_while_plugged_in").trim()
        try {
            select(DisplayMode.CLOCK)
            shell("settings put system screen_off_timeout 15000")
            shell("settings put global stay_on_while_plugged_in 0")
            capture("soak-start")
            val start = SystemClock.elapsedRealtime()
            repeat(minutes) { minute ->
                val target = start + (minute + 1) * 60000L
                SystemClock.sleep((target - SystemClock.elapsedRealtime()).coerceAtLeast(0))
                assertTrue("Screen went to sleep after ${minute + 1} minutes", shell("dumpsys power").contains("mWakefulness=Awake"))
                compose.onNodeWithTag("clock-text").assertIsDisplayed()
                compose.onNodeWithTag("phase-text").assertDoesNotExist()
                val values = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
                    .fetchSemanticsNodes().flatMap { it.config[SemanticsProperties.Text] }.map { it.text }
                val text = values.single { it.matches(Regex("\\d{2}:\\d{2}:\\d{2}")) }
                val parts = text.split(":").map { it.toInt() }
                val displayed = parts[0] * 3600 + parts[1] * 60 + parts[2]
                val now = Calendar.getInstance()
                val actual = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND)
                val difference = ((actual - displayed) + 86400) % 86400
                assertTrue("Clock is stale: $text", difference <= 2 || difference >= 86398)
                println("CLOCK_SOAK: ${minute + 1}/$minutes minutes passed")
            }
            capture("soak-end")
            shell("input keyevent KEYCODE_HOME")
            SystemClock.sleep(20000)
            assertFalse("Screen must be allowed to sleep after leaving", shell("dumpsys power").contains("mWakefulness=Awake"))
            shell("input keyevent KEYCODE_WAKEUP")
            shell("wm dismiss-keyguard")
        } finally {
            shell("settings put system screen_off_timeout $oldTimeout")
            shell("settings put global stay_on_while_plugged_in $oldCharging")
        }
    }
}
