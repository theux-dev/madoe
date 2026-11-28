package com.mangareader.app.data

import com.mangareader.app.data.model.AliasState
import com.mangareader.app.data.model.AppIcon
import com.mangareader.app.data.model.resolveCurrentIcon
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Confere a escolha do ícone do app: a lógica que descobre o ícone ativo e a consistência entre o
 * enum [AppIcon], o AndroidManifest.xml e os recursos. Os testes rodam na pasta do módulo `app`.
 */
class AppIconTest {
    private val androidNs = "http://schemas.android.com/apk/res/android"
    private val basePackage = "com.mangareader.app"

    private val manifest: Element by lazy {
        DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder().parse(File("src/main/AndroidManifest.xml")).documentElement
    }

    private fun Element.attr(name: String): String = getAttributeNS(androidNs, name)

    private fun Element.children(tag: String): List<Element> =
        getElementsByTagName(tag).let { list -> (0 until list.length).map { list.item(it) as Element } }

    private fun fullName(element: Element): String =
        element.attr("name").let { if (it.startsWith(".")) basePackage + it else it }

    private fun isLauncher(element: Element): Boolean = element.children("intent-filter").any { filter ->
        filter.children("action").any { it.attr("name") == "android.intent.action.MAIN" } &&
            filter.children("category").any { it.attr("name") == "android.intent.category.LAUNCHER" }
    }

    private val aliases: List<Element> get() = manifest.children("activity-alias")

    // --- lógica do ícone ativo ---

    @Test fun withNoStoredChoiceTheDefaultIconIsActive() {
        val states = AppIcon.entries.associateWith { AliasState.MANIFEST_DEFAULT }
        assertEquals(AppIcon.DEFAULT, resolveCurrentIcon(states))
    }

    @Test fun anExplicitlyEnabledIconWins() {
        val states = AppIcon.entries.associateWith { AliasState.DISABLED } + (AppIcon.SAKURA to AliasState.ENABLED)
        assertEquals(AppIcon.SAKURA, resolveCurrentIcon(states))
    }

    @Test fun anExplicitChoiceBeatsTheManifestDefault() {
        val states = mapOf(AppIcon.MADOE to AliasState.MANIFEST_DEFAULT, AppIcon.OCEAN to AliasState.ENABLED)
        assertEquals(AppIcon.OCEAN, resolveCurrentIcon(states))
    }

    @Test fun ifEverythingIsDisabledItFallsBackToTheDefault() {
        val states = AppIcon.entries.associateWith { AliasState.DISABLED }
        assertEquals(AppIcon.DEFAULT, resolveCurrentIcon(states))
    }

    // --- consistência com o manifesto ---

    @Test fun everyIconHasAnAliasInTheManifest() {
        val declared = aliases.map(::fullName)
        AppIcon.entries.forEach { assertTrue("${it.alias} não está no manifesto", it.alias in declared) }
    }

    @Test fun everyAliasInTheManifestIsAKnownIcon() {
        val known = AppIcon.entries.map { it.alias }
        aliases.forEach { assertTrue("${fullName(it)} está no manifesto mas não em AppIcon", fullName(it) in known) }
    }

    @Test fun onlyTheDefaultIconIsEnabledInTheManifest() {
        aliases.forEach {
            val shouldBeOn = fullName(it) == AppIcon.DEFAULT.alias
            val isOn = it.attr("enabled") != "false"
            assertEquals("estado inicial de ${fullName(it)}", shouldBeOn, isOn)
        }
    }

    @Test fun everyAliasIsAnExportedLauncherEntryOfTheMainActivity() {
        aliases.forEach {
            assertTrue("${fullName(it)} precisa da entrada MAIN/LAUNCHER", isLauncher(it))
            assertEquals("${fullName(it)} precisa ser exported", "true", it.attr("exported"))
            assertEquals("$basePackage.MainActivity", fullNameOfTarget(it))
        }
    }

    private fun fullNameOfTarget(alias: Element): String =
        alias.attr("targetActivity").let { if (it.startsWith(".")) basePackage + it else it }

    @Test fun theMainActivityIsNotALauncherEntryItself() {
        // Se fosse, o app apareceria duas vezes no launcher.
        manifest.children("activity").forEach { assertTrue("${fullName(it)} não pode ser launcher", !isLauncher(it)) }
    }

    @Test fun everyAliasHasItsIconResources() {
        val colors = File("src/main/res/values/colors.xml").readText()
        aliases.forEach { alias ->
            val mipmap = alias.attr("icon").removePrefix("@mipmap/")
            val file = File("src/main/res/mipmap-anydpi-v26/$mipmap.xml")
            assertTrue("falta ${file.path}", file.exists())
            val xml = file.readText()
            Regex("@drawable/([A-Za-z0-9_]+)").findAll(xml).forEach {
                val name = it.groupValues[1]
                assertTrue("$mipmap usa @drawable/$name, que não existe", File("src/main/res/drawable/$name.xml").exists())
            }
            Regex("@color/([A-Za-z0-9_]+)").findAll(xml).forEach {
                val name = it.groupValues[1]
                assertTrue("$mipmap usa @color/$name, que não existe", colors.contains("name=\"$name\""))
            }
            assertTrue("$mipmap precisa da versão de uma cor (monochrome)", xml.contains("<monochrome"))
        }
    }

    // --- nomes que nunca podem sumir ---

    @Test fun releasedAliasNamesStillExist() {
        // Se um nome daqui sumir, quem escolheu esse ícone fica sem atalho. Para acrescentar um
        // ícone novo, ponha o nome dele aqui também; nunca tire um.
        val released = listOf(
            "com.mangareader.app.icon.Madoe",
            "com.mangareader.app.icon.Paper",
            "com.mangareader.app.icon.Ocean",
            "com.mangareader.app.icon.Sakura",
        )
        val current = AppIcon.entries.map { it.alias }
        released.forEach { assertTrue("$it foi removido ou renomeado", it in current) }
        assertNotNull(AppIcon.DEFAULT)
    }
}
