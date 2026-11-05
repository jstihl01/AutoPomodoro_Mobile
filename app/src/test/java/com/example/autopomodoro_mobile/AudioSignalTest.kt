package com.jstihl01.autopomodoro.mobile

import java.io.DataOutputStream
import java.io.File
import kotlin.math.*
import org.junit.Assert.*
import org.junit.Test

class AudioSignalTest {
    @Test fun louderSignalHasHeadroomAndProducesAuditableComparison() {
        val directory = File("build/reports/audio").apply { mkdirs() }
        val reports = listOf(true, false).map { rest ->
            val old = legacySamples(rest)
            val current = ToneSamples.create(rest)
            val oldRms = rms(old)
            val newRms = rms(current)
            val ratio = newRms / oldRms
            assertEquals(old.size, current.size)
            assertTrue("RMS gain must be at least 2.5x", ratio >= 2.5)
            assertTrue("No samples may clip", current.all { abs(it.toInt()) < Short.MAX_VALUE })
            assertTrue("Keep at least 9% peak headroom", current.maxOf { abs(it.toInt()) } <= Short.MAX_VALUE * 0.91)
            val defaultRatio = newRms / (oldRms * 0.6)
            assertTrue("New default must be at least 4x former default amplitude", defaultRatio >= 4.0)
            val name = if (rest) "rest" else "work"
            writeWave(File(directory, "$name-before.wav"), old)
            writeWave(File(directory, "$name-after.wav"), current)
            "{\"tone\":\"$name\",\"oldRms\":$oldRms,\"newRms\":$newRms,\"amplitudeRatio\":$ratio," +
                "\"gainDb\":${20 * log10(ratio)},\"defaultAmplitudeRatio\":$defaultRatio," +
                "\"defaultGainDb\":${20 * log10(defaultRatio)},\"peak\":${current.maxOf { abs(it.toInt()) }},\"clippedSamples\":0}"
        }
        File(directory, "comparison.json").writeText("[${reports.joinToString(",\n")}]")
    }

    private fun rms(samples: ShortArray) = sqrt(samples.sumOf { it.toDouble() * it } / samples.size)

    // Reference waveform from mobile v1.0.0, independent of the new peak constant.
    private fun legacySamples(rest: Boolean): ShortArray {
        val length = 44100 * (if (rest) 700 else 170) / 1000
        val gap = 44100 * 130 / 1000
        val repeats = if (rest) 1 else 3
        val result = ShortArray(length * repeats + gap * (repeats - 1))
        val fade = minOf(44100 / 25, length / 3)
        repeat(repeats) { repeat ->
            for (i in 0 until length) {
                val envelope = minOf(1.0, i.toDouble() / fade, (length - i - 1).toDouble() / fade)
                result[repeat * (length + gap) + i] =
                    (sin(2 * PI * (if (rest) 523.25 else 659.25) * i / 44100) * envelope * 0.35 * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return result
    }

    private fun writeWave(file: File, samples: ShortArray) {
        DataOutputStream(file.outputStream()).use {
            it.writeBytes("RIFF"); it.writeInt(Integer.reverseBytes(36 + samples.size * 2)); it.writeBytes("WAVEfmt ")
            it.writeInt(Integer.reverseBytes(16)); it.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt())
            it.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()); it.writeInt(Integer.reverseBytes(44100))
            it.writeInt(Integer.reverseBytes(88200)); it.writeShort(java.lang.Short.reverseBytes(2.toShort()).toInt())
            it.writeShort(java.lang.Short.reverseBytes(16.toShort()).toInt()); it.writeBytes("data")
            it.writeInt(Integer.reverseBytes(samples.size * 2))
            samples.forEach { sample -> it.writeShort(java.lang.Short.reverseBytes(sample).toInt()) }
        }
    }
}
