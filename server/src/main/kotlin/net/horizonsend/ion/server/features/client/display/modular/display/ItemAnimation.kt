package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayWrapper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

class ItemAnimation(val itemDisplay: ItemDisplayWrapper, val duration: Long, val delayBeforeStart: Long) {
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
        for (keyframe in keyframes) {
            val delay = keyframe.key // from the start of the animation
            val frame = keyframe.value

            Tasks.asyncDelay(delay + delayBeforeStart) {
                if (frame.position != null) {
                    itemDisplay.position = frame.position
                }
                if (frame.heading != null) {
                    itemDisplay.heading = frame.heading
                }
                if (frame.offset != null) {
                    itemDisplay.offset = frame.offset
                }
                if (frame.interpolationDuration != null) {
                    itemDisplay.interpolationDuration = frame.interpolationDuration
                }
                if (frame.scale != null) {
                    itemDisplay.scale = frame.scale
                }
                itemDisplay.update()
            }

            Tasks.asyncDelay(duration + delayBeforeStart) {
                itemDisplay.remove()
            }
        }
    }
}