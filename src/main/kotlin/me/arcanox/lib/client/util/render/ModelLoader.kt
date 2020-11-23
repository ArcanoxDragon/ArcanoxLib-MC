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
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import net.minecraftforge.client.model.ModelLoader as ForgeModelLoader
import net.minecraft.util.Unit as MUnit

@Target(AnnotationTarget.CLASS)
annotation class ConsumesModels;

class LazyModel internal constructor(val modelPath: String) {
	private var modelCache: LazyCache<IBakedModel>? = null
	
	fun get(): IBakedModel = this.modelCache?.value ?: throwUninitialized()
	
	fun reload() {
		val modelCache = this.modelCache ?: throwUninitialized();
		
		modelCache.invalidate();
		modelCache.poke();
	}
	
	internal fun initialize(cache: LazyCache<IBakedModel>) {
		this.modelCache = cache
	}
	
	private fun throwUninitialized(): Nothing = throw UnsupportedOperationException("LazyModel accessed before it was initialized")
}

fun lazyModel(modelPath: String) = LazyModel(modelPath)

object ModelLoader {
	private class RegisteredModelConsumer(val consumerClass: KClass<*>) : IFutureReloadListener {
		val registeredModels = mutableListOf<LazyModel>();
		
		/**
		 * Reloads all of the models that were registered to this RegisteredModelConsumer at mod-load time.
		 *
		 * Reloading is done by invalidating and immediately poking the LazyCache that holds each model such
		 * that it is reloaded by Minecraft.
		 */
		override fun reload(stage: IFutureReloadListener.IStage, resourceManager: IResourceManager, preparationsProfiler: IProfiler,
		                    reloadProfiler: IProfiler, backgroundExecutor: Executor, gameExecutor: Executor): CompletableFuture<Void> =
			stage.markCompleteAwaitingOthers(MUnit.INSTANCE).thenRun {
				val logger = LogManager.getLogger();
				val className = this.consumerClass.simpleName;
				
				logger.debug("Beginning reload for ModelConsumer \"$className\"...");
				this.registeredModels.forEach { it.reload() };
				logger.debug("Reload is complete for ModelConsumer \"$className\".");
			}
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
			val modelConsumer = modelConsumerClass.objectInstance ?: modelConsumerClass.companionObjectInstance ?: run {
				logger.warn("Class \"$className\" has a ConsumesModels annotation, but it is not an object and does not have a companion object");
				return@forClassesWithAnnotation;
			};
			val consumerInfo = RegisteredModelConsumer(modelConsumerClass);
			
			// Find all declared properties on the IModelConsumer's class that are of type LazyModel so we can initialize their caches
			modelConsumer::class.declaredMemberProperties
				.filter { it.returnType.classifier == LazyModel::class }
				.forEach {
					@Suppress("UNCHECKED_CAST")
					val property = it as? KProperty1<Any, LazyModel> ?: run {
						logger.warn("LazyModel property \"${it.name}\" on class \"$className\" could not be accessed; model will not be registered");
						return@forEach;
					};
					val lazyModel = property.get(modelConsumer);
					val resourceLocation = ResourceLocation(modId, lazyModel.modelPath);
					val modelCache = lazyCache { Minecraft.getInstance().modelManager.getModel(resourceLocation) };
					
					// Initialize the LazyModel with our cache
					lazyModel.initialize(modelCache);
					
					// Register the model
					logger.info("Registering model \"${lazyModel.modelPath}\" for class \"$className\"...");
					ForgeModelLoader.addSpecialModel(resourceLocation);
					
					// Record this model for later reloading
					consumerInfo.registeredModels += lazyModel;
				};
			
			logger.info("Registered ${consumerInfo.registeredModels.size} models for class \"$className\"");
			
			// Keep track of the consumer for later reloading
			modelConsumers += consumerInfo;
			
			// Actually listen for reloads
			reloadableResourceManager?.addReloadListener(consumerInfo);
		};
		
		val totalModels = modelConsumers.sumBy { it.registeredModels.size };
		val totalConsumers = modelConsumers.size;
		
		logger.info("Model registration complete. Registered $totalModels models for $totalConsumers consumers.");
	}
}