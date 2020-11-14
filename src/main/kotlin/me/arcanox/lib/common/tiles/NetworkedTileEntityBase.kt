package me.arcanox.lib.common.tiles

import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SUpdateTileEntityPacket
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType

abstract class NetworkedTileEntityBase(type: TileEntityType<out TileEntity>) : TileEntityBase(type) {
	override fun getUpdatePacket(): SUpdateTileEntityPacket? {
		return SUpdateTileEntityPacket(this.pos, 1, this.updateTag);
	}
	
	override fun onDataPacket(net: NetworkManager, pkt: SUpdateTileEntityPacket) {
		this.handleUpdateTag(this.blockState, pkt.nbtCompound);
	}
}