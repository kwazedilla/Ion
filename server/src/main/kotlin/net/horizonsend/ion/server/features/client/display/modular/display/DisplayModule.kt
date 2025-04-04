package net.horizonsend.ion.server.features.client.display.modular.display

import net.minecraft.world.entity.Display
import org.bukkit.Location

interface DisplayModule {
    val entity: Display

    fun createEntity(): Display
    fun getLocation(): Location
    fun resetPosition()
    fun register()
    fun deRegister()
    fun runUpdates()
    fun remove()
}