package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ChargedProjectiles
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.NewBlasterBalancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.entityLoadCrossbowListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.playerSwapHandsListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.ModManager
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
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
            event.isCancelled = true
            fire(event.player, item)
        })

        addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@NewBlaster) { event, _, item ->
            event.isCancelled = true
        })

        addComponent(CustomComponentTypes.LISTENER_PLAYER_SWAP_HANDS, playerSwapHandsListener(this@NewBlaster) { event, _, item ->
            reload(event.player, item)
        })

        addComponent(CustomComponentTypes.LISTENER_ENTITY_LOAD_CROSSBOW, entityLoadCrossbowListener(this@NewBlaster) { event, _, item ->
            event.isCancelled = true
        })
    }

    open fun fire(shooter: LivingEntity, blasterItem: ItemStack) {
        fireProjectiles(shooter)
    }

    open fun fireProjectiles(livingEntity: LivingEntity) {
        val location = livingEntity.eyeLocation.clone()

        location.y -= 0.125

        if (balancing.spreadDegrees > 0) {
            val radians = balancing.spreadDegrees * Math.PI / 180
            location.direction = location.direction
                .rotateAroundX(randomDouble(-radians, radians))
                .rotateAroundY(randomDouble(-radians, radians))
                .rotateAroundZ(randomDouble(-radians, radians))
        }

        NewBlasterProjectile(
            location = location,
            shooter = livingEntity,
            balancing = balancing,
            particle = Particle.DUST
        ).fire()
    }

    open fun reload(livingEntity: LivingEntity, blasterItem: ItemStack) {

    }

    override fun decorateItemStack(base: ItemStack) {
        // Clear base item attributes
        base.updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
        base.updateData(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectiles.chargedProjectiles().add(ItemStack(Material.ARROW)).build())
    }

    abstract fun sendActionBar(audience: Audience)
}