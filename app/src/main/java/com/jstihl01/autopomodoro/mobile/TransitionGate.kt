package com.jstihl01.autopomodoro.mobile

import kotlin.math.abs

/** Only a continuously observed phase boundary may produce a sound. */
class TransitionGate {
    private var previous: Pair<TimeReading, DisplaySnapshot>? = null

    fun reset() { previous = null }

    fun observe(time: TimeReading, snapshot: DisplaySnapshot): Phase? {
        val last = previous
        previous = time to snapshot
        if (last == null || snapshot.phase == null) return null
        val (oldTime, oldSnapshot) = last
        val wallGap = time.wallMillis - oldTime.wallMillis
        val monotonicGap = time.monotonicMillis - oldTime.monotonicMillis
        return snapshot.phase.takeIf {
            oldSnapshot.mode == snapshot.mode && oldSnapshot.phase != null &&
                oldSnapshot.phase != snapshot.phase && oldTime.zone.id == time.zone.id &&
                wallGap in 0..2000 && monotonicGap in 0..2000 && abs(wallGap - monotonicGap) <= 250
        }
    }
}
