package com.jstihl01.autopomodoro.mobile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository by lazy { SettingsRepository(this) }
    private val engine = ClockEngine()
    private var settings by mutableStateOf<AppSettings?>(null)
    private var snapshot by mutableStateOf<DisplaySnapshot?>(null)
    private var settingsOpen by mutableStateOf(false)
    private val timeChanged = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { refresh() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            repository.settings.collect {
                settings = it
                refresh()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    refresh()
                    delay((1000 - System.currentTimeMillis() % 1000).coerceAtLeast(1))
                }
            }
        }
        setContent {
            AutoPomodoroTheme {
                val currentSettings = settings
                val currentSnapshot = snapshot
                if (currentSettings != null && currentSnapshot != null) {
                    ClockScreen(currentSnapshot, onSettings = { settingsOpen = true; applySystemBars() })
                    if (settingsOpen) SettingsDialog(
                        settings = currentSettings,
                        onMode = { mode -> lifecycleScope.launch { repository.setMode(mode) } },
                        onVolume = { volume -> lifecycleScope.launch { repository.setVolume(volume) } },
                        onTestSound = {},
                        onReset = { lifecycleScope.launch { repository.reset() } },
                        onDismiss = { settingsOpen = false; applySystemBars() },
                    )
                }
            }
        }
    }

    private fun refresh() {
        settings?.let { snapshot = engine.current(it.mode).second }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ContextCompat.registerReceiver(this, timeChanged, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
        refresh()
        applySystemBars()
    }

    override fun onPause() {
        unregisterReceiver(timeChanged)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applySystemBars()
    }

    private fun applySystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (settingsOpen) show(WindowInsetsCompat.Type.systemBars())
            else hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
