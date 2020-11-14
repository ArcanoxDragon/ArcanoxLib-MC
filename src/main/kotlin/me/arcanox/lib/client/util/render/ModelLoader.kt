package me.arcanox.lib.client.util.render

import me.arcanox.lib.util.LazyCache
import me.arcanox.lib.util.lazyCache
import me.arcanox.lib.util.reflect.ReflectionHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IFutureReloadListener
import net.minecraft.resources.IReloadableResourceManager
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible
import net.minecraftforge.client.model.ModelLoader as ForgeModelLoader

@Target(AnnotationTarget.CLASS)
annotation class ConsumesModels;

@Target(AnnotationTarget.PROPERTY)
annotation class ModelLocation(val resourceLocation: String);

object ModelLoader {
	private class RegisteredModelConsumer(val consumerClass: KClass<*>) : IFutureReloadListener {
		val registeredModels = mutableListOf<LazyCache<IBakedModel>>();
		
		/**
		 * Reloads all of the models that were registered to this RegisteredModelConsumer at mod-load time.
		 *
		 * Reloading is done by invalidating and immediately poking the LazyCache that holds each model such
		 * that it is reloaded by Minecraft.
		 */
		override fun reload(stage: IFutureReloadListener.IStage, resourceManager: IResourceManager, preparationsProfiler: IProfiler,
		                    reloadProfiler: IProfiler, backgroundExecutor: Executor, gameExecutor: Executor): CompletableFuture<Void> = CompletableFuture.runAsync {
			val logger = LogManager.getLogger();
			val className = this.consumerClass.simpleName;
			
			logger.debug("Beginning reload for ModelConsumer \"$className\"...");
			this.registeredModels.forEach {
				// Reload this model by invalidating and then poking the cache
				it.invalidate();
				it.poke();
			}
			logger.debug("Reload is complete for ModelConsumer \"$className\".");
		}.thenCompose(stage::markCompleteAwaitingOthers)
	}
	
	private val modelConsumers = mutableListOf<RegisteredModelConsumer>()
	
	fun registerModelsInPackage(modId: String, packagePrefix: String) {
		val logger = LogManager.getLogger();
		
		logger.info("Beginning model registration for mod \"$modId\" in package \"$packagePrefix\"...");
		
		val reloadableResourceManager = Minecraft.getInstance().resourceManager as? IReloadableResourceManager;
		
		// Find all IModelConsumers that reside in the provided packagePrefix
		logger.info("Scanning \"$packagePrefix\" for model consumers...")
		ReflectionHelper.forClassesWithAnnotation(ConsumesModels::class, Any::class, packagePrefix) { modelConsumerClass, _ ->
			val className = modelConsumerClass.simpleName;
			
			// Try and figure out what we're setting model properties on (either an object instance or a companion object instance)
			val modelConsumer = if (modelConsumerClass.objectInstance != null) {
				modelConsumerClass.objectInstance!!;
			} else {
				val companionObject = modelConsumerClass.companionObjectInstance;
				
				if (companionObject == null) {
					logger.warn("Class \"$className\" has a ConsumesModels annotation, but it is not an object and does not have a companion object");
					return@forClassesWithAnnotation;
				}
				
				modelConsumerClass.companionObjectInstance!!;
			}
			
			val consumerInfo = RegisteredModelConsumer(modelConsumerClass);
			
			// Find all declared properties on the IModelConsumer's class that are of type LazyCache<*> and that have a ModelLocation annotation
			modelConsumer.javaClass.kotlin.declaredMemberProperties
				.filter { it.returnType.classifier == LazyCache::class }
				.filter { it.hasAnnotation<ModelLocation>() }
				.forEach {
					if (it !is KMutableProperty1) {
						logger.warn("Model property \"${it.name}\" on class \"$className\" is not mutable; model will not be registered");
						return@forEach;
					}
					
					val modelLocationAnnotation = it.findAnnotation<ModelLocation>() ?: return@forEach;
					val resourceLocation = ResourceLocation(modId, modelLocationAnnotation.resourceLocation);
					val modelCache = lazyCache { Minecraft.getInstance().modelManager.getModel(resourceLocation) };
					
					// Cast "it" down so we can actually set it
					@Suppress("UNCHECKED_CAST")
					it as KMutableProperty1<Any, LazyCache<IBakedModel>>;
					it.setter.isAccessible = true;
					
					try {
						it.set(modelConsumer, modelCache);
					} catch (ex: Exception) {
						logger.error("Model property \"${it.name}\" on class \"$className\" could not be set during model registration");
						ex.printStackTrace();
						return@forEach;
					}
					
					// Register the model
					logger.info("Registering model \"${modelLocationAnnotation.resourceLocation}\" for class \"$className\"...");
					ForgeModelLoader.addSpecialModel(resourceLocation);
					
					// Record this model for later reloading
					consumerInfo.registeredModels += modelCache;
				};
			
			logger.info("Registered ${consumerInfo.registeredModels.size} models for class \"$className\"");
			
			modelConsumers += consumerInfo;
			
			// Listen for reloads
			reloadableResourceManager?.addReloadListener(consumerInfo);
		};
		
		val totalModels = modelConsumers.sumBy { it.registeredModels.size };
		val totalConsumers = modelConsumers.size;
		
		logger.info("Model registration complete. Registered $totalModels models for $totalConsumers consumers.");
	}
}