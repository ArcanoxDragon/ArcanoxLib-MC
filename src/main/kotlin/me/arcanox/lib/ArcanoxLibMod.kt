package me.arcanox.lib

import me.arcanox.lib.client.IClientInitHandler
import me.arcanox.lib.common.IInitHandler
import me.arcanox.lib.util.Logger
import me.arcanox.lib.util.reflect.ClientInitHandler
import me.arcanox.lib.util.reflect.InitHandler
import me.arcanox.lib.util.reflect.ReflectionHelper
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

@Mod(ArcanoxLibMod.ModID)
object ArcanoxLibMod {
	const val Name = "ArcanoxLib"
	const val ModID = "arcanoxlib"
	
	private val commonInitHandlers = emptyList<IInitHandler>().toMutableList();
	private val clientInitHandlers = emptyList<IClientInitHandler>().toMutableList();
	
	init {
		MOD_CONTEXT.getKEventBus().addListener(this::onCommonInit);
		MOD_CONTEXT.getKEventBus().addListener(this::onClientInit);
	}
	
	private fun onCommonInit(event: FMLCommonSetupEvent) {
		Logger.info("Beginning common initialization phase...");
		
		// Find all IInitHandler classes and allow them to initialize
		this.commonInitHandlers += ReflectionHelper
			.getInstancesWithAnnotation(InitHandler::class, IInitHandler::class)
			.sortedBy { it.second.priority }
			.map { it.first };
		this.commonInitHandlers.forEach { it.onInit(event) };
		
		Logger.info("Common initialization phase complete.");
	}
	
	private fun onClientInit(event: FMLClientSetupEvent) {
		Logger.info("Beginning client initialization phase...");
		
		// Find all IClientInitHandler classes and allow them to initialize
		this.clientInitHandlers += ReflectionHelper
			.getInstancesWithAnnotation(ClientInitHandler::class, IClientInitHandler::class)
			.sortedBy { it.second.priority }
			.map { it.first };
		this.clientInitHandlers.forEach { it.onClientInit(event) };
		
		Logger.info("Client initialization phase complete.");
	}
}