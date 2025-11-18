package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.kyori.adventure.sound.Sound

@Serializable
data class NewPVPBalancing(
    val blasterBalancing: BlasterBalancing = BlasterBalancing()
) {
    @Serializable
    data class BlasterBalancing(
        val testBlaster: TestBlasterBalancing = TestBlasterBalancing()
    )
}

@Serializable
sealed interface NewBlasterBalancing {
    val closeRangeDamage: Double
    val closeRangeDistanceThreshold: Double
    val longRangeDamage: Double
    val longRangeDistanceThreshold: Double
    val headshotMultiplier: Double
    val spreadDegrees: Double

    val projectileSize: Double
    val visualProjectileSize: Float
    val maxRange: Double

    val soundRange: Double
    val soundReloadStart: SoundInfo
    val soundReloadFinish: SoundInfo
    val soundFire: SoundInfo
    val soundWhizz: SoundInfo
    val soundShell: SoundInfo
}

@Serializable
sealed interface AmmoStoringBlasterBalancing: NewBlasterBalancing {
    val capacity: Int
    val displayDurability: Boolean
    val consumesAmmo: Boolean
}

@Serializable
sealed interface ProjectileBlasterBalancing : NewBlasterBalancing, AmmoStoringBlasterBalancing {
    val speed: Double
    val projectileDropAccel: Double
}

@Serializable
data class TestBlasterBalancing(
    override val closeRangeDamage: Double = 10.0,
    override val closeRangeDistanceThreshold: Double = 20.0,
    override val longRangeDamage: Double = 5.0,
    override val longRangeDistanceThreshold: Double = 40.0,
    override val headshotMultiplier: Double = 1.5,
    override val spreadDegrees: Double = 5.0,

    override val projectileSize: Double = 0.5,
    override val visualProjectileSize: Float = 0.25f,
    override val maxRange: Double = 500.0,

    override val soundRange: Double = 1.0,
    override val soundReloadStart: SoundInfo = SoundInfo("horizonsend:blaster.rifle.reload.start", volume = 1f, source = Sound.Source.PLAYER),
    override val soundReloadFinish: SoundInfo = SoundInfo("horizonsend:blaster.rifle.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
    override val soundFire: SoundInfo = SoundInfo("horizonsend:blaster.rifle.shoot", volume = 1f, source = Sound.Source.PLAYER),
    override val soundWhizz: SoundInfo = SoundInfo("horizonsend:blaster.whizz.standard", volume = 1f, source = Sound.Source.PLAYER),
    override val soundShell: SoundInfo = SoundInfo("horizonsend:blaster.rifle.shell", volume = 1f, source = Sound.Source.PLAYER),

    override val speed: Double = 375.0,
    override val projectileDropAccel: Double = 10.0,

    override val capacity: Int = 30,
    override val displayDurability: Boolean = true,
    override val consumesAmmo: Boolean = true
) : ProjectileBlasterBalancing