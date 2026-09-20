package studio.paras.medha.core.ui

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberAssetImage(fileName: String, trim: Boolean = false): ImageBitmap {
    val assets = LocalContext.current.assets
    return remember(fileName, trim) {
        assets.open(fileName).use { stream ->
            val opts = BitmapFactory.Options().apply { inScaled = false }
            val bmp = BitmapFactory.decodeStream(stream, null, opts)
                ?: error("Asset not found: $fileName")
            val out = if (trim) trimTransparent(bmp) else bmp
            out.asImageBitmap()
        }
    }
}

private fun trimTransparent(src: android.graphics.Bitmap): android.graphics.Bitmap {
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
    val pad = 8
    val l = (left - pad).coerceAtLeast(0)
    val t = (top - pad).coerceAtLeast(0)
    val r = (right + pad).coerceAtMost(w - 1)
    val b = (bottom + pad).coerceAtMost(h - 1)
    return android.graphics.Bitmap.createBitmap(src, l, t, r - l + 1, b - t + 1)
}
