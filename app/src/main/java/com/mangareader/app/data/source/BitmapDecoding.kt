package com.mangareader.app.data.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory

private const val MAX_DECODED_PIXELS = 24_000_000L

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

internal fun isImageName(name: String): Boolean =
    name.substringAfterLast('.', "").lowercase() in IMAGE_EXTENSIONS

/** Decodifica [bytes] usando o maior `inSampleSize` que ainda entrega pelo menos o tamanho pedido. */
internal fun decodeSampled(
    bytes: ByteArray,
    maxWidth: Int,
    maxHeight: Int,
    maxPixels: Long = MAX_DECODED_PIXELS,
): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        throw CorruptComicException("Imagem inválida ou corrompida")
    }
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxWidth && bounds.outHeight / (sample * 2) >= maxHeight) {
        sample *= 2
    }
    // Tiras muito longas (webtoons) não podem estourar a memória: reduz até caber no limite de pixels.
    while ((bounds.outWidth.toLong() / sample) * (bounds.outHeight.toLong() / sample) > maxPixels) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        ?: throw CorruptComicException("Não foi possível decodificar a imagem")
}
