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

// region Blocks

@Target(AnnotationTarget.CLASS)
annotation class ModBlock

@Target(AnnotationTarget.CLASS)
annotation class HasBlockItem(val maxStackSize: Int = 64)

@Target(AnnotationTarget.CLASS)
annotation class HasItemModel

// endregion

// region TileEntities

@Target(AnnotationTarget.CLASS)
annotation class ModTileEntity(val id: String, vararg val blocks: KClass<out Block>)

@Target(AnnotationTarget.CLASS)
annotation class HasTileEntityRenderer(val rendererClass: KClass<out TileEntityRenderer<*>>)

// endregion