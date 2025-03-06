package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

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
	item: ItemStack,
	initScale: Vector = Vector(1.0, 1.0, 1.0)
) {
	var scale: Vector = initScale
		set(value) {
			field = value
			updateTransformation(entity)
			playerManager.runUpdates()
		}

	var position: Vector = initPosition
		set(value) {
			field = value
			updateTransformation(entity)
			playerManager.sendTeleport()
		}

	var heading: Vector = initHeading
		set(value) {
			field = value
			updateTransformation(entity)
			playerManager.runUpdates()
		}

	var offset: Vector = initTransformation
		set(value) {
			field = value
			updateTransformation(entity)
			playerManager.runUpdates()
		}

	var interpolationDuration: Int = initInterpolationDuration
		set(value) {
			field = value
			updateInterpolation(entity)
			playerManager.runUpdates()
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
		teleportDuration = 0
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

	fun remove() {
		playerManager.sendRemove()
	}

	fun update() {
		playerManager.runUpdates()
	}

	fun getEntity() = entity
}
