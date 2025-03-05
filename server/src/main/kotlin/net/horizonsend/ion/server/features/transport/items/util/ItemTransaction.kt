package net.horizonsend.ion.server.features.transport.items.util

import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVector3f
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.entity.Display
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

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
		val players = originLocation.world.players
		for (player in players) {
			val entity = ClientDisplayEntityFactory.createItemDisplay(player)
			entity.setItemStack(transferredItem)
			entity.billboard = Display.Billboard.FIXED
			entity.viewRange = 5.0f
			entity.interpolationDuration = 20
			entity.transformation = Transformation(
				Vector3f(),
				Quaternionf(),
				Vector3f(0.75f),
				Quaternionf()
			)

			val nmsEntity = entity.getNMSData(originLocation.x, originLocation.y, originLocation.z)

			ClientDisplayEntities.sendEntityPacket(player, nmsEntity, 20L)

			/*
			Tasks.sync {
				val firstDestinationLocation = destinationInventories.firstEntry().value.location
				if (firstDestinationLocation != null) {
					val transformation = com.mojang.math.Transformation(
						firstDestinationLocation.toVector3f(),
						Quaternionf(),
						Vector3f(0.75f),
						Quaternionf()
					)
					ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
				}
			}
			 */
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
