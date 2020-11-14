package me.arcanox.lib.client

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent

/**
 * A class annotated with the ClientInitHandler annotation must implement this interface.
 *
 * The onClientInit function will be called during the correct mod initialization stage.
 */
interface IClientInitHandler {
	fun onClientInit(e: FMLClientSetupEvent);
}