package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayWrapper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class ItemAnimation(val item: ItemStack, private val originLocation: Location, val duration: Long, val delayBeforeStart: Long) {
    val keyframes = mutableMapOf<Long, Keyframe>()

    /**
     * Adds a movement keyframe. The item will move to the keyframe's specified position at a certain tick
     * after the animation starts playing.
     * @param tick the tick at which this keyframe should be active
     * @param keyframe the keyframe, detailing position/transformation/other information about a display
     */
    fun addKeyframe(tick: Long, keyframe: Keyframe) {
        keyframes += tick to keyframe
    }

    /**
     * Plays the animation.
     */
    fun play() {
        val itemDisplay = ItemDisplayWrapper(
            world = originLocation.world,
            initPosition = originLocation.toCenterLocation().toVector(),
            initHeading = Vector(),
            initTransformation = Vector(),
            initInterpolationDuration = 3,
            initTeleportDuration = 3,
            item = item,
            initScale = Vector(0.75, 0.75, 0.75)
        )

        Tasks.syncDelay(delayBeforeStart) {
            itemDisplay.update()
            println("CREATE ITEM: ${itemDisplay.itemStack.displayNameString}, ${itemDisplay.position}")
        }

        for (keyframe in keyframes) {
            val delay = keyframe.key // from the start of the animation
            val frame = keyframe.value

            Tasks.syncDelay(delay + delayBeforeStart) {
                //println("RUNNING KEYFRAME FOR ${itemDisplay.itemStack.displayNameString} at ${delay + delayBeforeStart} tick")
                if (frame.position != null) {
                    itemDisplay.position = frame.position
                }
                if (frame.heading != null) {
                    itemDisplay.heading = frame.heading
                }
                if (frame.offset != null) {
                    itemDisplay.offset = frame.offset
                    //println("OFFSET: ${itemDisplay.itemStack.displayNameString}, ${itemDisplay.offset}")
                }
                if (frame.interpolationDuration != null) {
                    itemDisplay.interpolationDuration = frame.interpolationDuration
                }
                if (frame.teleportDuration != null) {
                    itemDisplay.teleportDuration = frame.teleportDuration
                }
                if (frame.scale != null) {
                    itemDisplay.scale = frame.scale
                }
            }

            Tasks.syncDelay(duration + delayBeforeStart) {
                itemDisplay.remove()
                //println("DELETE ITEM: ${itemDisplay.itemStack.displayNameString}")
            }
        }
    }
}