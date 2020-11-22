package me.arcanox.lib.util

import me.arcanox.lib.util.reflect.ReflectionHelper
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager

fun findAndRegisterEventSubscriberObjects(packagePrefix: String) {
	val logger = LogManager.getLogger();
	
	ReflectionHelper.getClassesWithAnnotation(Mod.EventBusSubscriber::class, Any::class, packagePrefix)
		.filter { it.first.objectInstance != null }
		.forEach { (subscriberClass, attribute) ->
			val subscriber = subscriberClass.objectInstance ?: return;
			val eventBusName = when (attribute.bus) {
				Mod.EventBusSubscriber.Bus.FORGE -> "Forge"
				Mod.EventBusSubscriber.Bus.MOD -> "Mod"
			};
			
			logger.info("Registering ${subscriber::class.simpleName} instance to $eventBusName event bus");
			
			val eventBus = when (attribute.bus) {
				Mod.EventBusSubscriber.Bus.FORGE -> MinecraftForge.EVENT_BUS
				Mod.EventBusSubscriber.Bus.MOD -> FMLJavaModLoadingContext.get().modEventBus
			};
			
			eventBus.register(subscriber);
		}
}