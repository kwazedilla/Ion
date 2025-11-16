package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.NewBlasterBalancing
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.concurrent.TimeUnit
import kotlin.math.min

class NewBlasterProjectile(
    val location: Location,
    val shooter: Entity?,
    val balancing: NewBlasterBalancing,
    val particle: Particle
) {
    var ticks: Int = 0
    var lastTick: Long = 0
    var delta: Double = 0.0
    var distanceTravelled: Double = 0.0
    val dustOptions = Particle.DustOptions(Color.RED, balancing.visualProjectileSize)

    companion object {
        const val CHECK_INCREMENT = 0.1
    }

    fun fire() {
        lastTick = System.nanoTime()

        runnable {
            if (tick()) cancel()
        }.runTaskTimer(IonServer, 0L, 1L)
    }

    fun tick(): Boolean {
        if (!location.isChunkLoaded) return true

        delta = (System.nanoTime() - lastTick) / TimeUnit.SECONDS.toNanos(1).toDouble()
        var distanceToTravelThisTick = delta * balancing.speed

        while (distanceToTravelThisTick > 0) {
            val distanceIncrement = min(CHECK_INCREMENT, distanceToTravelThisTick)

            val rayTraceResult = location.world.rayTrace(
                location,
                location.direction.clone().normalize(),
                distanceIncrement,
                FluidCollisionMode.NEVER,
                true,
                balancing.projectileSize
            ) { player -> player != shooter }

            val hitBlock = rayTraceResult?.hitBlock
            if (hitBlock != null) {
                return true
            }

            val hitEntity = rayTraceResult?.hitEntity
            if (hitEntity != null && hitEntity is Damageable) {
                var hasHeadshot = false
                val hitPosition = rayTraceResult.hitPosition

                if (hitEntity is LivingEntity) {
                    if (balancing.headshotMultiplier > 0 && (hitEntity.eyeLocation.y - hitPosition.y) < (.3 * balancing.projectileSize)) {
                        hasHeadshot = true
                    }
                }

                return true
            }

            location.add(location.direction.clone().normalize().multiply(distanceIncrement))

            location.world.spawnParticle(Particle.DUST, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)

            distanceToTravelThisTick -= distanceIncrement
            distanceTravelled += distanceIncrement

            // projectile has traveled to its max range
            if (distanceTravelled >= balancing.maxRange) return true
        }

        lastTick = System.nanoTime()
        ticks++

        return false
    }
}