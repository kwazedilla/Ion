package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import net.horizonsend.ion.server.configuration.NewBlasterBalancing
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Entity
import java.util.concurrent.TimeUnit

class NewBlasterProjectile(
    val location: Location,
    val shooter: Entity?,
    val balancing: NewBlasterBalancing,
    val particle: Particle
) {
    var ticks: Int = 0
    var lastTick: Long = 0
    var delta: Double = 0.0

    fun fire() {
        lastTick = System.nanoTime()

        Tasks.syncRepeat(0L, 1L) {
            tick()
        }
    }

    fun tick() {
        delta = (System.nanoTime() - lastTick) / TimeUnit.SECONDS.toNanos(1).toDouble()

        lastTick = System.nanoTime()
        ticks++
    }
}