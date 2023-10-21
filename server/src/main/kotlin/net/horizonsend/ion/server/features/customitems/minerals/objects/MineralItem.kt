package net.horizonsend.ion.server.features.customitems.minerals.objects

import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

abstract class MineralItem(
    identifier: String,

    private val material: Material,
    private val customModelData: Int,
    val displayName: Component
) : CustomItem(identifier) {

    override fun constructItemStack(): ItemStack {
        return ItemStack(material).updateMeta {
            it.setCustomModelData(customModelData)
            it.displayName(displayName)
        }
    }
}