package studio.paras.medha.core.ui

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

/**
 * Loads a PNG from assets as an [ImageBitmap].
 *
 * The art files are 1000–2500 px, far bigger than they are drawn, so they are
 * shrunk to [maxSize] on the longest side while decoding, and kept in a shared
 * memory cache so a picture used on several screens is decoded only once.
 * This keeps memory low on cheaper phones and stops the stutter when popups open.
 */
@Composable
fun rememberAssetImage(fileName: String, trim: Boolean = false, maxSize: Int = DefaultMaxSize): ImageBitmap {
    val assets = LocalContext.current.assets
    return remember(fileName, trim, maxSize) { AssetImageCache.load(assets, fileName, trim, maxSize) }
}

/** Longest side, in pixels, for buttons and frames. Full-screen backgrounds pass a larger value. */
const val DefaultMaxSize = 1024
const val BackgroundMaxSize = 1920

object AssetImageCache {
    private val cache = object : LruCache<String, ImageBitmap>(cacheBytes()) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * 4
    }

    private fun cacheBytes(): Int {
        val max = Runtime.getRuntime().maxMemory()
        return (max / 6).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun load(assets: AssetManager, fileName: String, trim: Boolean, maxSize: Int): ImageBitmap {
        val key = "$fileName|$trim|$maxSize"
        cache.get(key)?.let { return it }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assets.open(fileName).use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / (sample * 2) >= maxSize) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inScaled = false
            inSampleSize = sample
        }
        val decoded = assets.open(fileName).use { BitmapFactory.decodeStream(it, null, opts) }
            ?: error("Asset not found: $fileName")
        val fitted = fit(decoded, maxSize)
        // Keep the same 8 px margin the art was laid out with, measured at the original size.
        val pad = ((8f * fitted.width) / bounds.outWidth.coerceAtLeast(1)).toInt().coerceAtLeast(1)
        val out = (if (trim) trimTransparent(fitted, pad) else fitted).asImageBitmap()
        cache.put(key, out)
        return out
    }

    private fun fit(src: Bitmap, maxSize: Int): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= maxSize * 5 / 4) return src
        val scale = maxSize.toFloat() / longest
        val w = (src.width * scale).toInt().coerceAtLeast(1)
        val h = (src.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(src, w, h, true)
        if (scaled !== src) src.recycle()
        return scaled
    }

    fun clear() = cache.evictAll()
}

private fun trimTransparent(src: Bitmap, pad: Int): Bitmap {
    val w = src.width
    val h = src.height
    val pixels = IntArray(w * h)
    src.getPixels(pixels, 0, w, 0, 0, w, h)
    var left = w
    var top = h
    var right = 0
    var bottom = 0
    for (y in 0 until h) {
        val row = y * w
        for (x in 0 until w) {
            if ((pixels[row + x] ushr 24) < 16) continue
            if (x < left) left = x
            if (y < top) top = y
            if (x > right) right = x
            if (y > bottom) bottom = y
        }
    }
    if (right < left || bottom < top) return src
    val l = (left - pad).coerceAtLeast(0)
    val t = (top - pad).coerceAtLeast(0)
    val r = (right + pad).coerceAtMost(w - 1)
    val b = (bottom + pad).coerceAtMost(h - 1)
    return Bitmap.createBitmap(src, l, t, r - l + 1, b - t + 1)
}
