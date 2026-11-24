package com.mangareader.app.data.source

import java.text.Normalizer
import java.util.Locale

/** Imagem de uma página exatamente como está no arquivo (ou renderizada, no caso de PDF). */
class OriginalPage(val bytes: ByteArray, val extension: String) {
    val mimeType: String get() = mimeForExtension(extension) ?: "application/octet-stream"
}

fun mimeForExtension(extension: String): String? = when (extension.lowercase(Locale.ROOT)) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "webp" -> "image/webp"
    "gif" -> "image/gif"
    "bmp" -> "image/bmp"
    else -> null
}

/** Descobre o tipo da imagem pelos primeiros bytes; devolve a extensão ou `null` se não reconhecer. */
fun sniffImageExtension(bytes: ByteArray): String? {
    fun at(i: Int) = bytes[i].toInt() and 0xFF
    fun ascii(from: Int, text: String) =
        bytes.size >= from + text.length && text.indices.all { bytes[from + it].toInt() == text[it].code }
    return when {
        bytes.size >= 3 && at(0) == 0xFF && at(1) == 0xD8 && at(2) == 0xFF -> "jpg"
        bytes.size >= 8 && at(0) == 0x89 && ascii(1, "PNG") && at(4) == 0x0D && at(5) == 0x0A -> "png"
        ascii(0, "RIFF") && ascii(8, "WEBP") -> "webp"
        ascii(0, "GIF8") -> "gif"
        ascii(0, "BM") -> "bmp"
        else -> null
    }
}

/**
 * Nome seguro para o arquivo salvo: título sem acentos nem símbolos, número da página com 3 dígitos
 * e a extensão. Ex.: "Nhô-Quim", página 7, jpg -> "Nho-Quim_p007.jpg".
 */
fun exportFileName(title: String, page: Int, extension: String): String {
    val plain = Normalizer.normalize(title, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
    val safe = plain.trim()
        .replace(Regex("[^A-Za-z0-9._-]+"), "_")
        .trim('_', '.', '-')
        .take(60)
        .ifEmpty { "madoe" }
    return String.format(Locale.ROOT, "%s_p%03d.%s", safe, page, extension.lowercase(Locale.ROOT))
}
