package me.arcanox.lib.util.reflect

import net.minecraft.block.Block
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
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
annotation class HasTileEntityRenderer(val rendererClass: KClass<out TileEntityRenderer<*>>)

// endregion