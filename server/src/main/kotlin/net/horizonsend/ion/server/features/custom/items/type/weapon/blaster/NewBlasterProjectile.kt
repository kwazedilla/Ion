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
import org.bukkit.util.Vector
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
    var dropVelocity: Double = 0.0

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
        val distanceToTravelThisTick = delta * balancing.speed
        var remainingDistanceToTravelThisTick = distanceToTravelThisTick
        val velocityToDropThisTick = delta * balancing.projectileDropAccel
        //var remainingDistanceToDropThisTick = distanceToDropThisTick

        while (remainingDistanceToTravelThisTick > 0) {
            // calculate distance to travel during this "micro tick". the max distance in this micro tick is CHECK_INCREMENT
            val distanceIncrement = min(CHECK_INCREMENT, remainingDistanceToTravelThisTick)
            // calculate the change in velocity during the micro tick; uses the ratio between the distance traveled between the micro tick and the total distance to travel during this tick
            val dropVelocityIncrement = velocityToDropThisTick * distanceIncrement / distanceToTravelThisTick
            dropVelocity -= dropVelocityIncrement
            // calculate the change in position due to drop during the micro tick
            val dropPositionIncrement = dropVelocity * delta * (distanceIncrement / distanceToTravelThisTick)

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

            val newLocationDelta = location.direction.clone().normalize().multiply(distanceIncrement)
            newLocationDelta.add(Vector(0.0, dropPositionIncrement, 0.0)).normalize().multiply(distanceIncrement)
            location.add(newLocationDelta)

            location.world.spawnParticle(Particle.DUST, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)

            remainingDistanceToTravelThisTick -= distanceIncrement
            distanceTravelled += distanceIncrement

            // projectile has traveled to its max range
            if (distanceTravelled >= balancing.maxRange) return true
        }

        lastTick = System.nanoTime()
        ticks++

        return false
    }
}