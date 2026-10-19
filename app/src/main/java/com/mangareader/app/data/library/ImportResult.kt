package com.mangareader.app.data.library

/** Resultado de uma importação ou varredura. */
data class ImportResult(
    val imported: Int = 0,
    val duplicates: Int = 0,
    /** Nomes dos arquivos em formato que ainda não sabemos ler. */
    val unsupported: List<String> = emptyList(),
    /** "nome: motivo" dos arquivos que falharam (corrompidos, protegidos, ilegíveis). */
    val failed: List<String> = emptyList(),
) {
    operator fun plus(other: ImportResult) = ImportResult(
        imported = imported + other.imported,
        duplicates = duplicates + other.duplicates,
        unsupported = unsupported + other.unsupported,
        failed = failed + other.failed,
    )
}

/** Estado exibido na interface enquanto a biblioteca importa ou escaneia. */
data class LibraryTaskState(
    val running: Boolean = false,
    val message: String? = null,
    val result: ImportResult? = null,
)
