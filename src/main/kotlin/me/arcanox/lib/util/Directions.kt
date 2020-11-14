package me.arcanox.lib.util

import net.minecraft.util.Direction

/**
 * Nicer way of accessing Direction.values since it's ambiguous in Kotlin
 */
object Directions {
	val All = enumValues<Direction>()
}