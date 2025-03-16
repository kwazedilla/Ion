package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayPlayerManager
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.World
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftItemDisplay
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf

class ItemDisplayWrapper(
	val world: World,
	initPosition: Vector,
	initHeading: Vector,
	initTransformation: Vector,
	initInterpolationDuration: Int,
	initTeleportDuration: Int,
	item: ItemStack,
	initScale: Vector = Vector(1.0, 1.0, 1.0)
) {
	var scale: Vector = initScale
		set(value) {
			field = value
			updateTransformation(entity)
			update()
		}

	var position: Vector = initPosition
		set(value) {
			field = value
			updateTransformation(entity)
			teleport()
		}

	var heading: Vector = initHeading
		set(value) {
			field = value
			updateTransformation(entity)
			update()
		}

	var offset: Vector = initTransformation
		set(value) {
			field = value
			updateTransformation(entity)
			update()
		}

	var interpolationDuration: Int = initInterpolationDuration
		set(value) {
			field = value
			updateInterpolation(entity)
			update()
		}

	var teleportDuration: Int = initTeleportDuration
		set(value) {
			field = value
			updateTeleportDuration(entity)
			update()
		}

	var itemStack: ItemStack = item
		set(value) {
			field = value
			entity.itemStack = CraftItemStack.asNMSCopy(itemStack)
		}

	private var entity: Display.ItemDisplay = createEntity().getNMSData(
		position.x,
		position.y,
		position.z
	)

	val playerManager = DisplayPlayerManager(entity)

	private fun createEntity(): CraftItemDisplay = CraftItemDisplay(
		IonServer.server as CraftServer,
		Display.ItemDisplay(EntityType.ITEM_DISPLAY, world.minecraft)
	).apply {
		billboard = org.bukkit.entity.Display.Billboard.FIXED
		teleportDuration = 5
		interpolationDuration = 5
		viewRange = 1000f
		brightness = org.bukkit.entity.Display.Brightness(15, 15)

		transformation = Transformation(
			offset.toVector3f(),
			ClientDisplayEntities.rotateToFaceVector2d(heading.toVector3f()),
			scale.toVector3f(),
			Quaternionf()
		)

		setItemStack(this@ItemDisplayWrapper.itemStack)
	}

	fun updatePosition(entity: Display.ItemDisplay) {
		entity.teleportTo(position.x, position.y, position.z)
		teleport()
		update()
	}

	fun updateTransformation(entity: Display.ItemDisplay) {
		entity.setTransformation(com.mojang.math.Transformation(
			offset.toVector3f(),
			ClientDisplayEntities.rotateToFaceVector2d(heading.toVector3f()),
			scale.toVector3f(),
			Quaternionf()
		))

		update()
	}

	fun updateInterpolation(entity: Display.ItemDisplay) {
		entity.transformationInterpolationDuration = interpolationDuration
		update()
	}

	fun updateTeleportDuration(entity: Display.ItemDisplay) {
		(entity.bukkitEntity as ItemDisplay).teleportDuration = teleportDuration
		update()
	}

	fun remove() {
		playerManager.sendRemove()
	}

	fun update() {
		playerManager.runUpdates()
	}

	fun teleport() {
		playerManager.sendTeleport()
	}

	fun getEntity() = entity
}
