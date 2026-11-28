package com.mangareader.app.data.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.mangareader.app.data.model.AliasState
import com.mangareader.app.data.model.AppIcon
import com.mangareader.app.data.model.resolveCurrentIcon
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Troca o ícone do app ligando um `<activity-alias>` do manifesto e desligando os outros.
 * O estado fica guardado pelo próprio Android, então não há nada para salvar no DataStore.
 */
class AppIconManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val packageManager: PackageManager get() = context.packageManager

    private fun component(icon: AppIcon) = ComponentName(context.packageName, icon.alias)

    private fun state(icon: AppIcon): AliasState =
        when (packageManager.getComponentEnabledSetting(component(icon))) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> AliasState.ENABLED
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> AliasState.MANIFEST_DEFAULT
            else -> AliasState.DISABLED
        }

    /** O ícone ativo agora. */
    fun current(): AppIcon = resolveCurrentIcon(AppIcon.entries.associateWith { state(it) })

    /**
     * Passa a usar [target]. O novo atalho é ligado antes de desligar os outros, para o app nunca
     * ficar sem nenhum atalho no launcher. Devolve `false` se o Android recusou a troca.
     */
    fun select(target: AppIcon): Boolean {
        return try {
            packageManager.setComponentEnabledSetting(
                component(target),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
            AppIcon.entries.filter { it != target }.forEach {
                packageManager.setComponentEnabledSetting(
                    component(it),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
