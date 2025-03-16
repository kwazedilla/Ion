package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import net.horizonsend.ion.server.features.client.display.modular.display.ItemAnimation
import net.horizonsend.ion.server.features.client.display.modular.display.Keyframe
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.World
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class ItemTransaction {
	private val transactions = mutableListOf<BackedItemTransaction>()

	fun addTransfer(
		sourceReference: ItemReference,
		destinationInventories: Long2ObjectRBTreeMap<CraftInventory>,
		transferredItem: ItemStack,
		transferredAmount: Int,
		destinationSelector: (Long2ObjectRBTreeMap<CraftInventory>) -> Pair<BlockKey, CraftInventory>
	) {
		transactions += BackedItemTransaction(sourceReference, transferredItem, transferredAmount, destinationInventories, destinationSelector)
	}

	fun commit(
		originKey: BlockKey,
		world: World,
		transferredItem: ItemStack,
		animationTickDelay: Long,
	) {
		transactions
			.filter { transaction -> transaction.check() }
			.forEach { t ->
				// all transfers should have finished after this
				val destinations = t.execute()

				handleAnimation(originKey, world, destinations, transferredItem, animationTickDelay)
			}
	}

	private fun handleAnimation(
		originKey: BlockKey,
		world: World,
		destinations: List<BlockKey>,
		transferredItem: ItemStack,
		animationTickDelay: Long
	) {
		// location of original extractor
		val originVector = toVec3i(originKey)
		val originLocation = originVector.toLocation(world)

		// successful destinations, where items were deposited
		for (destination in destinations) {
			// create a new animation for every container
			val animation = ItemAnimation(transferredItem, originLocation, 20L, animationTickDelay)

			val destinationVector = toVec3i(destination)
			val destinationLocation = destinationVector.toLocation(world)

			animation.addKeyframe(
				5L, Keyframe(
					offset = /*destinationLocation.toCenterLocation().toVector().subtract(
						originLocation.toCenterLocation().toVector()*/Vector(5, 5, 5)
				)
			)

			animation.play()
		}
	}

	fun checkAll(): Boolean {
		return transactions.all { transaction -> transaction.check() }
	}
}
