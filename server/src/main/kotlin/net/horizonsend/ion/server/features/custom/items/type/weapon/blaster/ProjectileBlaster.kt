package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import net.horizonsend.ion.server.configuration.ProjectileBlasterBalancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
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

    //val ammoComponent = AmmunitionStorage(balancingSupplier, balancing.consumesAmmo)
    //val magazineComponent = MagazineType(balancingSupplier, CustomItemKeys[balancing.magazineIdentifier] ?: error("No custom item type ${balancing.magazineIdentifier}"))

    /*override val customComponents: CustomItemComponentManager = super.customComponents.apply {
        addComponent(CustomComponentTypes.AMMUNITION_STORAGE, ammoComponent)
        if (balancing.consumesAmmo) addComponent(CustomComponentTypes.MAGAZINE_TYPE, magazineComponent)
    }*/

    override fun decorateItemStack(base: ItemStack) {
        super.decorateItemStack(base)
        //ammoComponent.setAmmo(base, this, balancing.capacity)
    }

    override fun sendActionBar(audience: Audience) {
        TODO("Not yet implemented")
    }
}