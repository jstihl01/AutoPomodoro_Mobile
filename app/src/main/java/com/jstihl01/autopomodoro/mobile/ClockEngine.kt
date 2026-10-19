package com.jstihl01.autopomodoro.mobile

import android.os.SystemClock
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class DisplayMode(val label: String) {
    STANDARD("25/5"), EXTENDED("50/10"), CLOCK("Reloj")
}

enum class Phase(val label: String) { WORK("TRABAJO"), REST("DESCANSO") }

data class DisplaySnapshot(
    val mode: DisplayMode,
    val text: String,
    val phase: Phase? = null,
    val remainingSeconds: Int? = null,
)

data class TimeReading(val wallMillis: Long, val monotonicMillis: Long, val zone: TimeZone)

fun interface TimeSource { fun read(): TimeReading }

object DeviceTimeSource : TimeSource {
    override fun read() = TimeReading(
        System.currentTimeMillis(), SystemClock.elapsedRealtime(), TimeZone.getDefault(),
    )
}

class ClockEngine(private val timeSource: TimeSource = DeviceTimeSource) {
    fun current(mode: DisplayMode): Pair<TimeReading, DisplaySnapshot> {
        val time = timeSource.read()
        return time to at(time.wallMillis, time.zone, mode)
    }

    companion object {
        fun at(wallMillis: Long, zone: TimeZone, mode: DisplayMode): DisplaySnapshot {
            val calendar = Calendar.getInstance(zone).apply { timeInMillis = wallMillis }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            if (mode == DisplayMode.CLOCK) {
                return DisplaySnapshot(mode, String.format(Locale.ROOT, "%02d:%02d:%02d", hour, minute, second))
            }
            val cycleMinutes = if (mode == DisplayMode.STANDARD) 30 else 60
            val workMillis = (if (mode == DisplayMode.STANDARD) 25 else 50) * 60_000L
            val position = (minute % cycleMinutes) * 60_000L + second * 1000L + calendar.get(Calendar.MILLISECOND)
            val phase = if (position < workMillis) Phase.WORK else Phase.REST
            val end = if (phase == Phase.WORK) workMillis else cycleMinutes * 60_000L
            val remaining = ((end - position + 999L) / 1000L).toInt()
            return DisplaySnapshot(mode, String.format(Locale.ROOT, "%02d:%02d", remaining / 60, remaining % 60), phase, remaining)
        }
    }
}
