package com.jstihl01.autopomodoro.mobile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.runtime.*
import org.junit.Rule
import org.junit.Test

class ClockScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun clockHasSecondsAndNoPhase() {
        compose.setContent { AutoPomodoroTheme { ClockScreen(DisplaySnapshot(DisplayMode.CLOCK, "23:59:58"), {}) } }
        compose.onNodeWithText("23:59:58").assertIsDisplayed()
        compose.onNodeWithTag("phase-text").assertDoesNotExist()
        compose.onNodeWithContentDescription("Ajustes").assertHasClickAction()
    }
    @Test fun settingsCanChangeMode() {
        compose.setContent {
            var settings by remember { mutableStateOf(AppSettings()) }
            AutoPomodoroTheme { SettingsDialog(settings, { settings = settings.copy(mode = it) }, {}, {}, {}, {}) }
        }
        compose.onNodeWithTag("mode-CLOCK").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithText("Probar volumen").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("RESTABLECER AJUSTES").performScrollTo().performClick()
        compose.onNodeWithText("¿Restablecer ajustes?").assertIsDisplayed()
        compose.onNodeWithText("Se seleccionará 25/5 y el volumen volverá al 100 %.").assertIsDisplayed()
    }
    @Test fun restLabelIsVisible() {
        compose.setContent { AutoPomodoroTheme { ClockScreen(DisplaySnapshot(DisplayMode.STANDARD, "05:00", Phase.REST, 300), {}) } }
        compose.onNodeWithText("05:00").assertIsDisplayed()
        compose.onNodeWithText("DESCANSO").assertIsDisplayed()
    }
}
