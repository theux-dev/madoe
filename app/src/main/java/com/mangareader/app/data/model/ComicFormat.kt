package com.mangareader.app.data.model

/** Formatos de arquivo reconhecidos pela biblioteca. O suporte de leitura é implementado na fase 2. */
enum class ComicFormat(val label: String) {
    CBZ("cbz"),
    CBR("cbr"),
    CB7("cb7"),
    CBT("cbt"),
    ZIP("zip"),
    PDF("pdf"),
    IMAGE("img");

    companion object {
        fun fromFileName(name: String): ComicFormat? =
            when (name.substringAfterLast('.', "").lowercase()) {
                "cbz" -> CBZ
                "cbr" -> CBR
                "cb7" -> CB7
                "cbt" -> CBT
                "zip" -> ZIP
                "pdf" -> PDF
                "jpg", "jpeg", "png", "webp" -> IMAGE
                else -> null
            }
    }
}
