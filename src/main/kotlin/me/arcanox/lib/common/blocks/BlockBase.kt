package me.arcanox.lib.common.blocks

import me.arcanox.lib.util.Logger
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.chunk.ChunkRenderCache
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk

abstract class BlockBase(name: String, properties: Properties) : Block(properties) {
	var apiName: String
		private set
	
	init {
		this.apiName = name;
		
		this.setRegistryName(name);
	}
	
	fun getTileEntitySafe(world: IBlockReader, pos: BlockPos, state: BlockState): TileEntity? {
		if (!this.hasTileEntity(state)) {
			Logger.error("Cannot get TileEntity for block ${this.registryName} because it does not have one")
			return null;
		}
		
		val tileEntity = when (world) {
			is ChunkRenderCache -> world.getTileEntity(pos, Chunk.CreateEntityType.CHECK)
			else                -> world.getTileEntity(pos)
		}
		
		if (tileEntity == null) {
			Logger.warn("Block ${this.registryName} at position $pos should have a TileEntity but it does not!");
			
			return when (world) {
				is World -> this.createTileEntity(state, world)!!.also { world.setTileEntity(pos, it) };
				else     -> throw IllegalStateException("Block ${this.registryName} at position $pos should have a TileEntity but it does not, and a TileEntity could not be created!")
			}
		}
		
		return tileEntity;
	}
}