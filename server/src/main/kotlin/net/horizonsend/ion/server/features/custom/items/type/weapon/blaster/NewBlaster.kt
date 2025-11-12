package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.server.configuration.NewBlasterBalancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.playerSwapHandsListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.ModManager
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

abstract class NewBlaster<T : NewBlasterBalancing>(
    key: IonRegistryKey<CustomItem, out CustomItem>,
    displayName: Component,
    private val modLimit: Int,
    itemFactory: ItemFactory,
    val model: String,
    private val balancingSupplier: Supplier<T>
) : CustomItem(key, displayName, itemFactory) {
    val balancing get() = balancingSupplier.get()

    override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
        addComponent(CustomComponentTypes.MOD_MANAGER, ModManager(modLimit))

        addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@NewBlaster) { event, _, item ->
            fire(event.player, item)
        })

        addComponent(CustomComponentTypes.LISTENER_PLAYER_SWAP_HANDS, playerSwapHandsListener(this@NewBlaster) { event, _, item ->
            reload(event.player, item)
        })
    }

    open fun fire(shooter: LivingEntity, blasterItem: ItemStack) {
        if (shooter is Player) {

        }

        fireProjectiles(shooter)
    }

    open fun fireProjectiles(livingEntity: LivingEntity) {
        val location = livingEntity.eyeLocation.clone()

        location.y -= 0.125

        if (balancing.spreadDegrees > 0) {
            val radians = balancing.spreadDegrees * Math.PI / 180
            location.direction.rotateAroundX(radians).rotateAroundY(radians).rotateAroundZ(radians)
        }

        /*
        RayTracedParticleProjectile(
            location,
            livingEntity,
            balancing,
            DUST,
            balancing.explosiveShot,
            DustOptions(
                getParticleColor(livingEntity),
                balancing.particleSize
            ),
            balancing.soundWhizz,
        ).fire()
         */
    }

    open fun reload(livingEntity: LivingEntity, blasterItem: ItemStack) {

    }

    override fun decorateItemStack(base: ItemStack) {
        // Clear base item attributes
        base.updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
    }

    abstract fun sendActionBar(audience: Audience)
}