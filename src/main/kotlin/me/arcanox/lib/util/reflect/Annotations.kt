package me.arcanox.lib.util.reflect

import net.minecraft.tileentity.TileEntity
import kotlin.reflect.KClass

// region Initialization

@Target(AnnotationTarget.CLASS)
annotation class InitHandler(val priority: Int = 5)

@Target(AnnotationTarget.CLASS)
annotation class ClientInitHandler(val priority: Int = 5)

// endregion

// region TileEntities

@Target(AnnotationTarget.CLASS)
annotation class ModTileEntity(val id: String, vararg val validBlocks: String)

@Target(AnnotationTarget.CLASS)
annotation class TileEntityRendererFor(vararg val tileEntityClasses: KClass<out TileEntity>)

// endregion