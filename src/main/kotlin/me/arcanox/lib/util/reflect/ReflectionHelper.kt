package me.arcanox.lib.util.reflect

import me.arcanox.lib.util.Logger
import net.minecraftforge.fml.ModList
import org.objectweb.asm.Type
import java.lang.reflect.AnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

inline fun <reified T : Annotation> AnnotatedElement.hasAnnotation(declaredOnly: Boolean = true): Boolean = when {
	declaredOnly -> this.getDeclaredAnnotation(T::class.java)
	else         -> this.getAnnotation(T::class.java)
} != null

inline fun <reified T : Annotation> KClass<*>.hasAnnotation(declaredOnly: Boolean = true): Boolean = this.java.hasAnnotation<T>(declaredOnly)

inline fun <reified T : Annotation> Any.classHasAnnotation(declaredOnly: Boolean = true): Boolean = this.javaClass.hasAnnotation<T>(declaredOnly)

object ReflectionHelper {
	/**
	 * Searches all loaded classes for classes which are annotated with the specified annotation, and which inherit from
	 * the specified base class.
	 *
	 * Returns a list of pairs, the first item of each pair being the class itself and the second item being the annotation.
	 */
	fun <C, A : Annotation> getClassesWithAnnotation(annotationClass: Class<A>, supertype: Class<C>/*, packageFilter: String*/): List<Pair<Class<out C>, A>> {
		val annotationType = Type.getType(annotationClass);
		val allScanData = ModList.get().allScanData;
		
		// Flat-map all scan data into a list of all discovered annotations
		return allScanData
			.flatMap { scanData ->
				scanData.annotations
					.filter { it.annotationType == annotationType }
					.map { annotationData ->
						val className = annotationData.memberName;
						val typed: Class<out C>;
						
						try {
							val discoveredClass = Class.forName(className);
							
							typed = discoveredClass.asSubclass(supertype);
						} catch (ex: ClassNotFoundException) {
							Logger.warn("Could not retrieve class handle for class in ASM data table: $className")
							return@map null;
						} catch (ex: Exception) {
							Logger.warn("Found a class with annotation ${annotationClass.canonicalName} of type $className, which cannot be converted to requested type ${supertype.canonicalName}");
							return@map null;
						}
						
						val annotation = typed.getDeclaredAnnotation(annotationClass);
						
						if (annotation == null) {
							Logger.warn("Class $className has annotation ${annotationClass.canonicalName} through inheritance, but the annotation is not declared explicitly on the class");
							return@map null;
						}
						
						Pair(typed, annotation);
					}
			}.filterNotNull();
	}
	
	/**
	 * Searches all loaded Kotlin classes for classes which are annotated with the specified annotation, and which inherit
	 * from the specified base class.
	 *
	 * Returns a list of pairs, the first item of each pair being the class itself and the second item being the annotation.
	 */
	fun <C : Any, A : Annotation> getClassesWithAnnotation(annotationClass: KClass<A>,
	                                                       supertype: KClass<out C>): List<Pair<KClass<out C>, A>> =
		getClassesWithAnnotation(annotationClass.java, supertype.java).map { (c, a) -> Pair(c.kotlin, a) }
	
	/**
	 * Searches all loaded classes for classes which are annotated with the specified annotation, and which inherit from
	 * the specified base class.
	 *
	 * The provided action will be called for each class that is found matching the provided criteria.
	 */
	fun <C : Any, A : Annotation> forClassesWithAnnotation(annotationClass: Class<A>, supertype: Class<C>, action: (Class<out C>, A) -> Unit) =
		getClassesWithAnnotation(annotationClass, supertype).forEach { (clazz, annotation) -> action(clazz, annotation) }
	
	/**
	 * Searches all loaded Kotlin classes for classes which are annotated with the specified annotation, and which inherit
	 * from the specified base class.
	 *
	 * The provided action will be called for each class that is found matching the provided criteria.
	 */
	fun <C : Any, A : Annotation> forClassesWithAnnotation(annotationClass: KClass<A>, supertype: KClass<C>, action: (KClass<out C>, A) -> Unit) =
		getClassesWithAnnotation(annotationClass, supertype).forEach { (clazz, annotation) -> action(clazz, annotation) }
	
	/**
	 * Searches all loaded classes for classes which are annotated with the specified annotation, and which inherit from
	 * the specified base class. For all classes found that match the specified criteria, a new instance is created using
	 * the provided factory method, which defaults to calling the default constructor.
	 *
	 * Returns a list of pairs, the first item of each pair being the instance itself and the second item being the annotation.
	 */
	fun <C, A : Annotation> getInstancesWithAnnotation(annotationClass: Class<A>,
	                                                   supertype: Class<C>,
	                                                   new: (Class<out C>) -> C = { it.getDeclaredConstructor().newInstance() }): List<Pair<C, A>> =
		getClassesWithAnnotation(annotationClass, supertype).map { (it, annotation) ->
			try {
				val instance = new(it);
				
				return@map Pair(instance, annotation);
			} catch (ex: Exception) {
				Logger.error("Error instantiating class from ASM data table: ${it.canonicalName}");
				ex.printStackTrace();
			}
			
			return@map null;
		}.filterNotNull();
	
	/**
	 * Searches all loaded Kotlin classes for classes which are annotated with the specified annotation, and which inherit
	 * from the specified base class. For all classes found that match the specified criteria, an instance is obtained using
	 * the provided factory method, which defaults to grabbing the objectInstance (if the class is an object) or invoking the
	 * default constructor for classes that are not objects.
	 *
	 * Returns a list of pairs, the first item of each pair being the instance itself and the second item being the annotation.
	 */
	fun <C : Any, A : Annotation> getInstancesWithAnnotation(annotationClass: KClass<A>,
	                                                         supertype: KClass<C>,
	                                                         new: (KClass<out C>) -> C = { it.objectInstance ?: it.createInstance() }): List<Pair<C, A>> =
		getInstancesWithAnnotation(annotationClass.java, supertype.java) { jc -> new(jc.kotlin) }
	
	/**
	 * Searches all loaded classes for classes which are annotated with the specified annotation, and which inherit from
	 * the specified base class. For all classes found that match the specified criteria, a new instance is created using
	 * the provided factory method, which defaults to calling the default constructor.
	 *
	 * The provided action will be called for each instance that can be obtained matching the provided criteria.
	 */
	fun <C : Any, A : Annotation> forInstancesWithAnnotation(annotationClass: Class<A>, supertype: Class<C>,
	                                                         new: (Class<out C>) -> C = { it.getDeclaredConstructor().newInstance() },
	                                                         action: (C, A) -> Unit) =
		getInstancesWithAnnotation(annotationClass, supertype, new).forEach { (instance, annotation) -> action(instance, annotation) }
	
	/**
	 * Searches all loaded Kotlin classes for classes which are annotated with the specified annotation, and which inherit
	 * from the specified base class. For all classes found that match the specified criteria, an instance is obtained using
	 * the provided factory method, which defaults to grabbing the objectInstance (if the class is an object) or invoking the
	 * default constructor for classes that are not objects.
	 *
	 * The provided action will be called for each instance that can be obtained matching the provided criteria.
	 */
	fun <C : Any, A : Annotation> forInstancesWithAnnotation(annotationClass: KClass<A>, supertype: KClass<C>,
	                                                         new: (KClass<out C>) -> C = { it.objectInstance ?: it.createInstance() },
	                                                         action: (C, A) -> Unit) =
		getInstancesWithAnnotation(annotationClass, supertype, new).forEach { (instance, annotation) -> action(instance, annotation) }
}