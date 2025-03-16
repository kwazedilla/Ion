package net.horizonsend.ion.server.features.client.display.modular.display

import org.bukkit.util.Vector

data class Keyframe(
    val position: Vector? = null,
    val heading: Vector? = null,
    val offset: Vector? = null,
    val interpolationDuration: Int? = null,
    val teleportDuration: Int? = null,
    val scale: Vector? = null,
)