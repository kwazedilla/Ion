package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import net.horizonsend.ion.server.configuration.ProjectileBlasterBalancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.NewAmmunitionStorage
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source.PLAYER
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

open class ProjectileBlaster(
    key: IonRegistryKey<CustomItem, out CustomItem>,
    displayName: Component,
    modLimit: Int,
    itemFactory: ItemFactory,
    model: String,
    balancingSupplier: Supplier<ProjectileBlasterBalancing>
) : NewBlaster<ProjectileBlasterBalancing>(key, displayName, modLimit, itemFactory, model, balancingSupplier) {

    val ammoComponent = NewAmmunitionStorage(balancingSupplier, balancing.consumesAmmo)
    //val magazineComponent = MagazineType(balancingSupplier, CustomItemKeys[balancing.magazineIdentifier] ?: error("No custom item type ${balancing.magazineIdentifier}"))

    override val customComponents: CustomItemComponentManager = super.customComponents.apply {
        addComponent(CustomComponentTypes.NEW_AMMUNITION_STORAGE, ammoComponent)
        //if (balancing.consumesAmmo) addComponent(CustomComponentTypes.MAGAZINE_TYPE, magazineComponent)
    }

    override fun decorateItemStack(base: ItemStack) {
        super.decorateItemStack(base)
        ammoComponent.setAmmo(base, this, balancing.capacity)
    }

    override fun sendActionBar(audience: Audience) {
        TODO("Not yet implemented")
    }

    override fun fire(shooter: LivingEntity, blasterItem: ItemStack) {
        if (shooter is Player) {
            if (!removeAmmo(blasterItem, shooter)) return

            super.fire(shooter, blasterItem)
        }
    }

    private fun removeAmmo(itemStack: ItemStack, livingEntity: LivingEntity, amount: Int = 1): Boolean {
        val ammo = ammoComponent.getAmmo(itemStack)
        if (amount > ammo) {
            livingEntity.playSound(sound(key("horizonsend:blaster.dry_shoot"), PLAYER, 1.0f, 1.0f))
            return false
        }

        ammoComponent.setAmmo(itemStack, this, ammo - amount)
        return true
    }
}