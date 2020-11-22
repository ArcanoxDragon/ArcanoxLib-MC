package me.arcanox.lib

import me.arcanox.lib.client.IClientInitHandler
import me.arcanox.lib.common.IInitHandler
import me.arcanox.lib.util.extensions.addKtListener
import me.arcanox.lib.util.findAndRegisterEventSubscriberObjects
import me.arcanox.lib.util.reflect.ClientInitHandler
import me.arcanox.lib.util.reflect.InitHandler
import me.arcanox.lib.util.reflect.ReflectionHelper
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager

abstract class ArcanoxModBase {
	private val commonInitHandlers = mutableListOf<IInitHandler>();
	private val clientInitHandlers = mutableListOf<IClientInitHandler>();
	
	abstract val modId: String
	abstract val packagePrefix: String
	
	protected fun finalizeInit() {
		// Add the mod's init handlers to the mod event bus
		FMLJavaModLoadingContext.get().modEventBus.addKtListener(this::onCommonInit);
		FMLJavaModLoadingContext.get().modEventBus.addKtListener(this::onClientInit);
		
		// Find any EventBusSubscriber objects and register them
		findAndRegisterEventSubscriberObjects(this.packagePrefix);
	}
	
	protected open fun onCommonInit(event: FMLCommonSetupEvent) {
		val logger = LogManager.getLogger();
		
		logger.info("Beginning common initialization phase for mod \"$modId\"...");
		
		// Find all IInitHandler classes and allow them to initialize
		this.commonInitHandlers.clear();
		this.commonInitHandlers += ReflectionHelper
			.getInstancesWithAnnotation(InitHandler::class, IInitHandler::class, this.packagePrefix)
			.sortedBy { it.second.priority }
			.map { it.first };
		this.commonInitHandlers.forEach { it.onInit(event) };
		
		logger.info("Common initialization phase for mod \"$modId\" complete.");
	}
	
	protected open fun onClientInit(event: FMLClientSetupEvent) {
		val logger = LogManager.getLogger();
		
		logger.info("Beginning client initialization phase for mod \"$modId\"...");
		
		// Find all IClientInitHandler classes and allow them to initialize
		this.clientInitHandlers.clear();
		this.clientInitHandlers += ReflectionHelper
			.getInstancesWithAnnotation(ClientInitHandler::class, IClientInitHandler::class, this.packagePrefix)
			.sortedBy { it.second.priority }
			.map { it.first };
		this.clientInitHandlers.forEach { it.onClientInit(event) };
		
		logger.info("Client initialization phase for mod \"$modId\" complete.");
	}
}