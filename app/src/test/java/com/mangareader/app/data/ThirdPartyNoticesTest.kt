package com.mangareader.app.data

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Garante que os avisos de terceiros mostrados no app (res/raw) e o THIRD_PARTY_NOTICES.md
 * continuam citando os componentes que exigem aviso. Os testes rodam na pasta do módulo `app`.
 */
class ThirdPartyNoticesTest {
    private val required = listOf(
        "Commons Compress", "junrar", "XZ for Java", "Coil", "Dagger", "SLF4J",
        "Bowlby One", "UnRAR", "Apache-2.0",
    )

    @Test fun inAppNoticesCiteEveryRequiredComponent() {
        val text = File("src/main/res/raw/third_party_notices.txt").readText()
        required.forEach { assertTrue("faltou \"$it\" no texto do app", text.contains(it)) }
    }

    @Test fun repositoryNoticesCiteEveryRequiredComponent() {
        val text = File("../THIRD_PARTY_NOTICES.md").readText()
        required.forEach { assertTrue("faltou \"$it\" no THIRD_PARTY_NOTICES.md", text.contains(it)) }
    }

    @Test fun licenseTextsAreBundledInTheApp() {
        listOf("license_apache_2.txt", "license_ofl_bowlby_one.txt", "license_unrar_junrar.txt").forEach {
            assertTrue("$it está vazio ou ausente", File("src/main/res/raw/$it").length() > 500)
        }
    }
}
