package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ItemDisplayWrapper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
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

	fun playTransferAnimation(
		originKey: BlockKey,
		world: World,
		destinationInventories: Long2ObjectRBTreeMap<CraftInventory>,
		transferredItem: ItemStack,
		tickDelay: Long,
	) {
		val originVector = toVec3i(originKey)
		val originLocation = originVector.toLocation(world)

		for (destinationInventory in destinationInventories) {
			val itemDisplayWrapper = ItemDisplayWrapper(
				world = originLocation.world,
				initPosition = originLocation.toCenterLocation().toVector(),
				initHeading = Vector(),
				initTransformation = Vector(),
				initInterpolationDuration = 10,
				item = transferredItem,
				initScale = Vector(0.75, 0.75, 0.75)
			)
			itemDisplayWrapper.update()

			// Notes: 1L delay seems to result in very inconsistent animations (specifically the lack of animations).
			// 2L may be the minimum to guarantee offset interpolation.
			Tasks.asyncDelay(2L + tickDelay) {
				val destinationLocation = destinationInventory.value.location
				if (destinationLocation != null) {
					itemDisplayWrapper.offset = destinationLocation.toCenterLocation().toVector().subtract(originLocation.toCenterLocation().toVector())
					itemDisplayWrapper.update()
				}
			}

			Tasks.syncDelay(20L + tickDelay) {
				itemDisplayWrapper.remove()
			}
		}
	}

	fun commit() {
		transactions
			.filter { transaction -> transaction.check() }
			.forEach { t -> t.execute() }
	}

	fun checkAll(): Boolean {
		return transactions.all { transaction -> transaction.check() }
	}
}
