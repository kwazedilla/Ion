package net.horizonsend.ion.server.features.starship.hyperspace

import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.PilotedStarships.getDisplayName
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.World
import kotlin.math.abs
import kotlin.math.sqrt

object MassShadows {
	const val PLANET_RADIUS = 1000
	const val STAR_RADIUS = 1800

	data class MassShadowInfo(val description: Component, val x: Int, val z: Int, val radius: Int, val distance: Int)

	fun find(world: World, x: Double, z: Double): MassShadowInfo? {
		val realWorld = (if (Hyperspace.isHyperspaceWorld(world)) Hyperspace.getRealspaceWorld(world) else world)
			?: return null

		for (planet in Space.getPlanets()) {
			if (planet.spaceWorld != realWorld) continue

			val loc = planet.location
			var dist = distanceSquared(x, 128.0, z, loc.x.toDouble(), 128.0, loc.z.toDouble())

			if (dist > PLANET_RADIUS.squared()) continue

			dist = sqrt(dist)
			return MassShadowInfo(text("Planet ${planet.name}"), loc.x, loc.z, PLANET_RADIUS, dist.toInt())
		}

		for (star in Space.getStars()) {
			if (star.spaceWorld != realWorld) continue

			val loc = star.location
			var dist = distanceSquared(x, 128.0, z, loc.x.toDouble(), 128.0, loc.z.toDouble())

			if (dist > STAR_RADIUS.squared()) continue

			dist = sqrt(dist)

			return MassShadowInfo(text("Star ${star.name}"), loc.x, loc.z, STAR_RADIUS, dist.toInt())
		}

		for (otherShip in ActiveStarships.getInWorld(realWorld)) {
			if (!ActiveStarships.isActive(otherShip)) continue
			if (!otherShip.isInterdicting) continue

			val otherX = otherShip.centerOfMass.x
			val otherY = otherShip.centerOfMass.y
			val otherZ = otherShip.centerOfMass.z
			var dist = distanceSquared(x, 128.0, z, otherX.toDouble(), otherY.toDouble(), otherZ.toDouble())

			if (dist > otherShip.interdictionRange.squared()) continue

			dist = sqrt(dist)

			val interdictingShip = otherShip as? ActiveControlledStarship
			val interdictingShipName = interdictingShip?.data?.let { getDisplayName(it) } ?: otherShip.type.displayNameComponent

			return MassShadowInfo(
				text()
					.append(interdictingShipName)
					.append(text(" piloted by "))
					.append(interdictingShip?.controller?.getPilotName() ?: text("none"))
					.build(),
				otherX,
				otherZ,
				otherShip.interdictionRange,
				dist.toInt()
			)
		}

		return null
	}

	/**
	 * Checks if a starship would intersect a gravity well.
	 * @param world the world the jump is taking place in
	 * @param x0 origin jump x
	 * @param z0 origin jump z
	 * @param x1 destination x
	 * @param z1 destination z
	 */
	fun check(world: World, x0: Double, z0: Double, x1: Double, z1: Double): MassShadowInfo? {
		val realWorld = (if (Hyperspace.isHyperspaceWorld(world)) Hyperspace.getRealspaceWorld(world) else world)
			?: return null

		for (planet in Space.getPlanets()) {
			if (planet.spaceWorld != realWorld) continue

			val loc = planet.location

			if (checkIntersect(x0, z0, x1, z1, loc.x.toDouble(), loc.z.toDouble(), PLANET_RADIUS)) {
				return MassShadowInfo(text("Planet ${planet.name}"), loc.x, loc.z, PLANET_RADIUS, PLANET_RADIUS)
			}
		}

		for (star in Space.getStars()) {
			if (star.spaceWorld != realWorld) continue

			val loc = star.location

			if (checkIntersect(x0, z0, x1, z1, loc.x.toDouble(), loc.z.toDouble(), STAR_RADIUS)) {
				return MassShadowInfo(text("Star ${star.name}"), loc.x, loc.z, STAR_RADIUS, STAR_RADIUS)
			}
		}

		return null
	}

	/**
	 * Checks if the line along two points would intersect a circle.
	 * @param x0 point 0 x
	 * @param z0 point 0 z
	 * @param x1 point 1 x
	 * @param z1 point 1 z
	 * @param xp circle origin x
	 * @param zp circle origin z
	 * @param radius circle radius
	 */
	private fun checkIntersect(x0: Double, z0: Double, x1: Double, z1: Double, xp: Double, zp: Double, radius: Int): Boolean {
		// y = mx + int -> ax + by + c = 0
		// a = -m, b = 1, c = -int
		val slope = (z1 - z0) / (x1 - x0)
		val zIntercept = z1 - (slope * x0)

		// (ax + by + c) / sqrt(a^2 + b^2)
		val dist = (abs(-slope * xp + zp - zIntercept)) / sqrt(slope * slope + 1)

		return if (radius >= dist) true else false
	}
}
