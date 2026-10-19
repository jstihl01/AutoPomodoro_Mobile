package com.jstihl01.autopomodoro.mobile

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test fun defaultsSaveReopenClampAndReset() = runBlocking {
        val file = File(temporaryFolder.root, "settings.preferences_pb")
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        fun repository() = SettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
        var repository = repository()
        try {
            assertEquals(AppSettings(), repository.settings.first())
            repository.setMode(DisplayMode.CLOCK)
            repository.setVolume(137)
            assertEquals(AppSettings(DisplayMode.CLOCK, 100), repository.settings.first())
            scope.coroutineContext.job.cancelAndJoin()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            repository = repository()
            assertEquals(AppSettings(DisplayMode.CLOCK, 100), repository.settings.first())
            repository.setVolume(-5)
            assertEquals(0, repository.settings.first().volumePercent)
            repository.reset()
            assertEquals(AppSettings(), repository.settings.first())
        } finally {
            scope.coroutineContext.job.cancelAndJoin()
        }
    }
}
