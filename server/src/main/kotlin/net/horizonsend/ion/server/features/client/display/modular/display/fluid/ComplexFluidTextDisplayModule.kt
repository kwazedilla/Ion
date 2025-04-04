package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline

class ComplexFluidTextDisplayModule(
	handler: TextDisplayHandler,
	container: StorageContainer,
	val title: Component,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float
) : FluidTextDisplayModule(handler, container, offsetLeft, offsetUp, offsetBack, scale) {

	override fun buildText(): Component {
		return ofChildren(title, newline(), formatFluid())
	}
}
