package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.minecraft.world.level.block.entity.BlockEntity
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack

class BackedItemTransaction(
	val source: ItemReference,
	val item: ItemStack,
	val amount: Int,
	val destinations: Long2ObjectRBTreeMap<CraftInventory>,
	val destinationSelector: (Long2ObjectRBTreeMap<CraftInventory>) -> Pair<BlockKey, CraftInventory>
) {
	private val successfulInventoryBlockKeys = mutableListOf<BlockKey>()

	fun check(): Boolean {
		return true
	}

	fun execute(): List<BlockKey> {
		val cloned = source.inventory.getItem(source.index)?.clone() ?: return successfulInventoryBlockKeys
		val notRemoved = tryRemove()

		val limit = amount - notRemoved

		if (limit <= 0) return successfulInventoryBlockKeys

		val notAdded = addToDestination(limit)
		if (notAdded <= 0) return successfulInventoryBlockKeys

		source.inventory.setItem(source.index, cloned.asQuantity(notAdded))
		return successfulInventoryBlockKeys
	}

	// Returns amount that could not be removed
	private fun tryRemove(): Int {
		val sourceStack = source.inventory.getItem(source.index) ?: return amount
		val removeAmount = minOf(amount, sourceStack.amount)

		val nmsContainer = source.inventory.inventory
		if (nmsContainer is BlockEntity) {
			nmsContainer.setChanged()
		}

		return if (amount == removeAmount) {
			source.inventory.setItem(source.index, null)

			0
		} else {
			sourceStack.amount -= removeAmount

			amount - removeAmount
		}
	}

	// Returns amount that did not fit
	private fun addToDestination(limit: Int): Int {
		var remaining = limit

		var destinationsRemaining = destinations.size

		while (remaining > 0 && destinationsRemaining >= 1) {
			destinationsRemaining--
			val destination = destinationSelector(destinations)
			val remainder = addToInventory(destination.second, item.asQuantity(remaining))
			successfulInventoryBlockKeys += destination.first

			if (remainder == 0) return 0
			remaining -= (remaining - remainder)
			destinations.remove(destination.first)
		}

		return remaining
	}
}
