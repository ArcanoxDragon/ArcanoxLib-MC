package me.arcanox.lib.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3i

/**
 * Returns the coordinates of this BlockPos as a Vector3d instance
 */
fun BlockPos.toVector3d() = Vector3d(this.x.toDouble(), this.y.toDouble(), this.z.toDouble());

/**
 * Returns the coordinates of this BlockPos as a Vector3i instance
 */
fun BlockPos.toVector3i() = Vector3i(this.x, this.y, this.z);