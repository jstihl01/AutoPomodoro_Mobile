package com.jstihl01.autopomodoro.mobile

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class ClockEngineTest {
    private val zone = TimeZone.getTimeZone("Europe/Madrid")
    private fun time(hour: Int, minute: Int, second: Int = 0, millis: Int = 0): Long =
        Calendar.getInstance(zone).apply {
            clear(); set(2026, Calendar.OCTOBER, 19, hour, minute, second); set(Calendar.MILLISECOND, millis)
        }.timeInMillis

    @Test fun standardBoundaries() {
        listOf(0 to 1500, 25 to 300, 30 to 1500, 55 to 300).forEach { (minute, remaining) ->
            val result = ClockEngine.at(time(14, minute), zone, DisplayMode.STANDARD)
            assertEquals(remaining, result.remainingSeconds)
            assertEquals(if (minute == 25 || minute == 55) Phase.REST else Phase.WORK, result.phase)
        }
    }

    @Test fun extendedBoundaries() {
        assertEquals(3000, ClockEngine.at(time(14, 0), zone, DisplayMode.EXTENDED).remainingSeconds)
        assertEquals(Phase.WORK, ClockEngine.at(time(14, 25), zone, DisplayMode.EXTENDED).phase)
        assertEquals(600, ClockEngine.at(time(14, 50), zone, DisplayMode.EXTENDED).remainingSeconds)
        assertEquals(3000, ClockEngine.at(time(15, 0), zone, DisplayMode.EXTENDED).remainingSeconds)
    }

    @Test fun fractionsRoundUpAndNeverShowZero() {
        for (mode in listOf(DisplayMode.STANDARD, DisplayMode.EXTENDED)) {
            val minute = if (mode == DisplayMode.STANDARD) 24 else 49
            assertEquals("00:01", ClockEngine.at(time(14, minute, 59, 999), zone, mode).text)
            assertEquals("00:01", ClockEngine.at(time(23, 59, 59, 999), zone, mode).text)
        }
    }

    @Test fun midnightAndClockHaveNoPhase() {
        val snapshot = ClockEngine.at(time(0, 0), zone, DisplayMode.CLOCK)
        assertEquals("00:00:00", snapshot.text)
        assertNull(snapshot.phase)
        assertNull(snapshot.remainingSeconds)
        assertEquals("25:00", ClockEngine.at(time(0, 0), zone, DisplayMode.STANDARD).text)
    }

    @Test fun injectedSourceAndZoneChanges() {
        var reading = TimeReading(time(14, 27, 12), 0, zone)
        val engine = ClockEngine { reading }
        assertEquals("14:27:12", engine.current(DisplayMode.CLOCK).second.text)
        assertEquals("02:48", engine.current(DisplayMode.STANDARD).second.text)
        assertEquals("22:48", engine.current(DisplayMode.EXTENDED).second.text)
        reading = reading.copy(zone = TimeZone.getTimeZone("UTC"))
        assertEquals("12:27:12", engine.current(DisplayMode.CLOCK).second.text)
    }
}
