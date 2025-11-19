package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.server.configuration.AmmoStoringBlasterBalancing
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.util.StoredValues
import net.horizonsend.ion.server.features.custom.items.util.updateDurability
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class NewAmmunitionStorage(val balancingSupplier: Supplier<out AmmoStoringBlasterBalancing>, private val consumesAmmo: Boolean = true) : CustomItemComponent, LoreManager {
    override val priority: Int = 201
    override fun shouldIncludeSeparator(): Boolean = false

    override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {
        StoredValues.AMMO.setAmount(baseItem, balancingSupplier.get().capacity)
    }

    override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> {
        return listOf(StoredValues.AMMO.formatLore(StoredValues.AMMO.getAmount(itemStack), balancingSupplier.get().capacity).itemLore)
    }

    override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> {
        return listOf()
    }

	fun setAmmo(itemStack: ItemStack, customItem: CustomItem, amount: Int) {
		val corrected = amount.coerceAtMost(balancingSupplier.get().capacity)

		StoredValues.AMMO.setAmount(itemStack, corrected)
		customItem.refreshLore(itemStack)

		if (balancingSupplier.get().displayDurability) updateDurability(itemStack, corrected, balancingSupplier.get().capacity)
	}

	fun getAmmo(itemStack: ItemStack): Int {
		return StoredValues.AMMO.getAmount(itemStack)
	}
}