package me.arcanox.lib.common.tiles

import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType

abstract class TileEntityBase(type: TileEntityType<out TileEntity>) : TileEntity(type)