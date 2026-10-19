package com.jstihl01.autopomodoro.mobile

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.sin

object ToneSamples {
    const val SAMPLE_RATE = 44100

    fun create(rest: Boolean, test: Boolean = false): ShortArray {
        val duration = if (rest) 700 else 170
        val repeats = if (rest || test) 1 else 3
        val toneLength = SAMPLE_RATE * duration / 1000
        val gapLength = SAMPLE_RATE * 130 / 1000
        val samples = ShortArray(toneLength * repeats + gapLength * (repeats - 1))
        val frequency = if (rest) 523.25 else 659.25
        val fadeLength = minOf(SAMPLE_RATE / 25, toneLength / 3)
        repeat(repeats) { repeat ->
            val offset = repeat * (toneLength + gapLength)
            for (index in 0 until toneLength) {
                val envelope = minOf(1.0, index.toDouble() / fadeLength, (toneLength - index - 1).toDouble() / fadeLength)
                samples[offset + index] = (sin(2 * PI * frequency * index / SAMPLE_RATE) * envelope * 0.35 * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return samples
    }
}

/** All AudioTrack ownership changes run on the activity's main coroutine scope. */
class TransitionSoundPlayer(private val scope: CoroutineScope) {
    private var job: Job? = null
    private var activeTrack: AudioTrack? = null

    fun play(phase: Phase, volumePercent: Int, test: Boolean = false) {
        stop()
        if (volumePercent <= 0) return
        job = scope.launch {
            var ownedTrack: AudioTrack? = null
            try {
                val samples = withContext(Dispatchers.Default) { ToneSamples.create(phase == Phase.REST, test) }
                ensureActive()
                val track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    .setAudioFormat(AudioFormat.Builder().setSampleRate(ToneSamples.SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(samples.size * 2).build()
                activeTrack = track
                ownedTrack = track
                track.setVolume(volumePercent.coerceIn(0, 100) / 100f)
                track.write(samples, 0, samples.size)
                track.play()
                delay(samples.size * 1000L / ToneSamples.SAMPLE_RATE + 50)
            } catch (error: IllegalArgumentException) {
                Log.w("AutoPomodoro", "Audio unavailable", error)
            } catch (error: IllegalStateException) {
                Log.w("AutoPomodoro", "Audio unavailable", error)
            } finally {
                if (ownedTrack != null && activeTrack === ownedTrack) releaseTrack()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        releaseTrack()
    }

    private fun releaseTrack() {
        activeTrack?.let { track ->
            activeTrack = null
            try { track.stop() } catch (_: IllegalStateException) { }
            track.release()
        }
    }
}
