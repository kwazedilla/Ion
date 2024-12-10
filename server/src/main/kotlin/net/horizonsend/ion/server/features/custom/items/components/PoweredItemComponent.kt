package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.BonusPowerAttribute
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.objects.StoredValues
import net.horizonsend.ion.server.features.custom.items.powered.CratePlacer.displayDurability
import net.horizonsend.ion.server.features.custom.items.powered.CratePlacer.updateDurability
import net.horizonsend.ion.server.features.custom.items.powered.PoweredItem.PowerLoreManager.powerPrefix
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Damageable
import org.bukkit.inventory.ItemStack

class PoweredItemComponent(private val baseMaxPower: Int) : CustomItemComponent, LoreManager {

	override fun decorateBase(baseItem: ItemStack) {
		StoredValues.POWER.setAmount(baseItem, baseMaxPower)
	}

	fun getMaxPower(customItem: NewCustomItem, itemStack: ItemStack): Int {
		return baseMaxPower + customItem.getAttributes(itemStack).filterIsInstance<BonusPowerAttribute>().sumOf { it.amount }
	}

	fun setPower(customItem: NewCustomItem, itemStack: ItemStack, amount: Int) {
		val capacity = getMaxPower(customItem, itemStack)
		val corrected = amount.coerceAtMost(capacity)

		StoredValues.POWER.setAmount(itemStack, corrected)

		if (displayDurability && itemStack.itemMeta is Damageable) {
			updateDurability(itemStack, corrected, capacity)
		}
	}

	fun getPower(itemStack: ItemStack): Int {
		return StoredValues.POWER.getAmount(itemStack)
	}

	override val priority: Int = 100
	override fun shouldIncludeSeparator(): Boolean = true

	override fun getLines(customItem: NewCustomItem, itemStack: ItemStack): List<Component> {
		val power = getPower(itemStack)

		return listOf(ofChildren(
			powerPrefix,
			text(power, HEColorScheme.HE_LIGHT_GRAY),
			text(" / ", HEColorScheme.HE_MEDIUM_GRAY),
			text(getMaxPower(customItem, itemStack), HEColorScheme.HE_LIGHT_GRAY)
		).decoration(TextDecoration.ITALIC, false))
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = mutableListOf()
}
