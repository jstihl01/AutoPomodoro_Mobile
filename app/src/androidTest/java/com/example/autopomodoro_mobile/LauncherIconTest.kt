package com.jstihl01.autopomodoro.mobile

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

class LauncherIconTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun launcherBackgroundHasNoTransparentOrBlackFringes() {
        for (id in listOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher_round)) {
            val icon = requireNotNull(context.getDrawable(id))
            val background = if (Build.VERSION.SDK_INT >= 26 && icon is AdaptiveIconDrawable) {
                icon.background
            } else {
                (icon as LayerDrawable).getDrawable(0)
            }
            val bitmap = render(background)
            try {
                for (y in 0 until bitmap.height step 3) for (x in 0 until bitmap.width step 3) {
                    val pixel = bitmap.getPixel(x, y)
                    assertEquals("Transparent background at $x,$y", 255, Color.alpha(pixel))
                    assertTrue("Black fringe at $x,$y", maxOf(Color.red(pixel), Color.green(pixel), Color.blue(pixel)) > 80)
                }
            } finally { bitmap.recycle() }
        }
    }

    @Test fun adaptiveClockStaysInsideTheLaunchersSafeZone() {
        if (Build.VERSION.SDK_INT < 26) return
        val icon = requireNotNull(context.getDrawable(R.mipmap.ic_launcher)) as AdaptiveIconDrawable
        val bitmap = render(icon.foreground)
        try {
            var painted = 0
            for (y in 0 until 108) for (x in 0 until 108) {
                if (Color.alpha(bitmap.getPixel(x, y)) > 20) {
                    painted++
                    assertTrue("Foreground outside safe zone: $x,$y", x in 21..86 && y in 21..86)
                }
            }
            assertTrue("Missing clock foreground", painted > 100)
        } finally { bitmap.recycle() }
    }

    private fun render(drawable: Drawable): Bitmap {
        val copy = requireNotNull(drawable.constantState).newDrawable(context.resources).mutate()
        return Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888).also {
            copy.setBounds(0, 0, 108, 108)
            copy.draw(Canvas(it))
        }
    }
}
