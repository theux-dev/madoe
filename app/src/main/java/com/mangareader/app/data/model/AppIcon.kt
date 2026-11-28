package com.mangareader.app.data.model

/**
 * Ícones do app que o usuário pode escolher. Cada um corresponde a um `<activity-alias>` do
 * AndroidManifest.xml, e [alias] é o nome completo dele.
 *
 * ATENÇÃO: o Android guarda esse nome. Depois de usado, não renomeie nem remova um item, senão
 * quem o escolheu fica sem ícone na tela inicial. Para criar outro: novo alias no manifesto, novo
 * item aqui, um mipmap `ic_launcher_<nome>.xml` e os textos em strings.xml.
 */
enum class AppIcon(val alias: String) {
    MADOE("com.mangareader.app.icon.Madoe"),
    PAPER("com.mangareader.app.icon.Paper"),
    OCEAN("com.mangareader.app.icon.Ocean"),
    SAKURA("com.mangareader.app.icon.Sakura"),
    ;

    companion object {
        /** O ícone que vem ligado no manifesto. */
        val DEFAULT = MADOE
    }
}

/** Estado de um atalho no Android, sem depender das constantes do PackageManager. */
enum class AliasState {
    ENABLED,
    DISABLED,

    /** O Android não guardou nenhuma escolha: vale o que o manifesto diz (só o padrão vem ligado). */
    MANIFEST_DEFAULT,
}

/**
 * Descobre qual ícone está ativo a partir do estado de cada atalho. Uma escolha explícita
 * ([AliasState.ENABLED]) vale mais que o estado do manifesto; se nada estiver ligado, cai no padrão,
 * para a interface nunca ficar sem uma opção marcada.
 */
fun resolveCurrentIcon(states: Map<AppIcon, AliasState>): AppIcon =
    AppIcon.entries.firstOrNull { states[it] == AliasState.ENABLED } ?: AppIcon.DEFAULT
