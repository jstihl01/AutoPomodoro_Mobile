package com.jstihl01.autopomodoro.mobile

import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class TransitionGateTest {
    private val zone = TimeZone.getTimeZone("UTC")
    private fun reading(wall: Long, mono: Long = wall) = TimeReading(wall, mono, zone)
    private fun snapshot(phase: Phase, mode: DisplayMode = DisplayMode.STANDARD) = DisplaySnapshot(mode, "00:01", phase, 1)

    @Test fun onlyContinuousBoundarySoundsOnce() {
        val gate = TransitionGate()
        assertNull(gate.observe(reading(1000), snapshot(Phase.WORK)))
        assertEquals(Phase.REST, gate.observe(reading(2000), snapshot(Phase.REST)))
        assertNull(gate.observe(reading(2001), snapshot(Phase.REST)))
        assertEquals(Phase.WORK, gate.observe(reading(3000), snapshot(Phase.WORK)))
    }

    @Test fun resetModeClockAndZoneChangesAreSilent() {
        val gate = TransitionGate()
        gate.observe(reading(1000), snapshot(Phase.WORK))
        gate.reset()
        assertNull(gate.observe(reading(2000), snapshot(Phase.REST)))
        assertNull(gate.observe(reading(2500), snapshot(Phase.WORK, DisplayMode.EXTENDED)))
        assertNull(gate.observe(reading(3000), DisplaySnapshot(DisplayMode.CLOCK, "12:00:00")))
        assertNull(gate.observe(reading(3500), snapshot(Phase.REST)))
        assertNull(gate.observe(reading(4000).copy(zone = TimeZone.getTimeZone("Europe/Madrid")), snapshot(Phase.WORK)))
    }

    @Test fun backgroundGapsAndClockCorrectionsAreSilent() {
        listOf(5000L to 5000L, 500L to 2000L, 2500L to 2000L, 1900L to 1000L).forEach { (wall, mono) ->
            val gate = TransitionGate()
            gate.observe(reading(1000), snapshot(Phase.WORK))
            assertNull(gate.observe(reading(wall, mono), snapshot(Phase.REST)))
        }
    }

    @Test fun everyRealBoundaryIsDetected() {
        listOf(DisplayMode.STANDARD to listOf(25, 30, 55, 60), DisplayMode.EXTENDED to listOf(50, 60)).forEach { (mode, minutes) ->
            minutes.forEach { minute ->
                val gate = TransitionGate()
                val wall = minute * 60000L
                gate.observe(reading(wall - 1), ClockEngine.at(wall - 1, zone, mode))
                val after = ClockEngine.at(wall, zone, mode)
                assertEquals(after.phase, gate.observe(reading(wall), after))
            }
        }
    }

    @Test fun samplesHaveSilentEdgesAndCorrectToneCounts() {
        val rest = ToneSamples.create(rest = true)
        val work = ToneSamples.create(rest = false)
        val test = ToneSamples.create(rest = false, test = true)
        assertEquals(30870, rest.size)
        assertEquals(33957, work.size)
        assertEquals(7497, test.size)
        listOf(rest, work, test).forEach {
            assertEquals(0.toShort(), it.first()); assertEquals(0.toShort(), it.last())
            assertTrue(it.any { sample -> sample != 0.toShort() })
        }
        assertTrue(work.sliceArray(7497 until 13230).all { it == 0.toShort() })
    }
}
