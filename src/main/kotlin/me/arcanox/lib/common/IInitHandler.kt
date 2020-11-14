package me.arcanox.lib.common

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent

/**
 * A class annotated with the InitHandler annotation must implement this interface.
 *
 * The onInit function will be called during the correct mod initialization stage.
 */
interface IInitHandler {
	fun onInit(e: FMLCommonSetupEvent);
}