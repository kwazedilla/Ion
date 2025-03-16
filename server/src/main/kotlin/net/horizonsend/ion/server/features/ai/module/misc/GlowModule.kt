package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayWrapper
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class GlowModule(controller: AIController) : net.horizonsend.ion.server.features.ai.module.AIModule(controller) {
	val container = ItemDisplayWrapper(
		world,
		starship.centerOfMass.toCenterVector(),
		BlockFace.UP.direction,
		Vector(),
		0,
		0,
		ItemStack(Material.JUKEBOX),
		Vector(1.5f, 1.5f, 1.5f)
	).apply {
		getEntity().setGlowingTag(true)
	}

	override fun onMove(movement: StarshipMovement) {
		container.position = starship.centerOfMass.toCenterVector()

		container.update()
	}

	private var ticks = 0

	override fun tick() {
		ticks++

		if (ticks % 20 != 0) return

		container.update()
	}

	override fun shutDown() {
		container.remove()
	}
}
