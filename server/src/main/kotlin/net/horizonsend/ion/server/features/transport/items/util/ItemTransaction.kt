package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ItemDisplayWrapper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
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

	fun addAnimation(
		sourceReference: ItemReference,
		destinationInventories: Long2ObjectRBTreeMap<CraftInventory>,
		transferredItem: ItemStack,
	) {
		val originLocation = sourceReference.inventory.location ?: return
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
		Tasks.asyncDelay(2L) {
			val firstDestination = destinationInventories.firstEntry().value.location
			if (firstDestination != null) {
				itemDisplayWrapper.offset = firstDestination.toCenterLocation().toVector().subtract(originLocation.toCenterLocation().toVector())
				itemDisplayWrapper.update()
			}
			/*
			itemDisplayWrapper.offset = itemDisplayWrapper.offset.clone().add(Vector(0, 5, 0))
			itemDisplayWrapper.update()
			 */
		}

		Tasks.syncDelay(20L) {
			itemDisplayWrapper.remove()
		}

		/*
		val players = originLocation.world.players
		for (player in players) {
			val entity = ClientDisplayEntityFactory.createItemDisplay(player)
			entity.setItemStack(transferredItem)
			entity.viewRange = 5.0f
			entity.interpolationDuration = 10
			entity.interpolationDelay = 0
			entity.transformation = Transformation(
				Vector3f(),
				Quaternionf(),
				Vector3f(0.75f),
				Quaternionf()
			)
			val nmsEntity = entity.getNMSData(originLocation.x + 0.5, originLocation.y + 0.5, originLocation.z + 0.5)

			Tasks.sync {
				ClientDisplayEntities.sendEntityPacket(player, nmsEntity, 20L)
			}

			val firstDestinationLocation = destinationInventories.firstEntry().value.location
			if (firstDestinationLocation != null) {
				val offset = Vector3f(
					(firstDestinationLocation.x - originLocation.x).toFloat(),
					(firstDestinationLocation.y - originLocation.y).toFloat(),
					(firstDestinationLocation.z - originLocation.z).toFloat()
				)
				val transformation = com.mojang.math.Transformation(
					offset,
					Quaternionf(),
					Vector3f(0.75f),
					Quaternionf()
				)

				Tasks.syncDelayTask(1L) {
					ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
				}
			}
		}
		 */
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
