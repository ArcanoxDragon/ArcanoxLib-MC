package me.arcanox.lib.util

/**
 * Creates a new LazyCache<T> with the provided valueSupplier
 */
fun <T> lazyCache(valueSupplier: () -> T) = LazyCache(valueSupplier);

/**
 * A LazyCache is an implementation of a late-init pattern. When its value is
 * accessed for the first time, it will call the valueSupplier it was created
 * with to obtain its value instance, and it will then save that instance and
 * return it for any future access to the value property.
 */
class LazyCache<T> internal constructor(private val valueSupplier: () -> T) {
	private var internalValue: T? = null;
	
	var initialized: Boolean = false
		private set;
	
	/**
	 * Gets the value represented by this LazyCache, initializing it if it has
	 * not already been initialized (or if the cache has been invalidated)
	 */
	val value: T
		get() {
			this.poke();
			
			return this.internalValue!!;
		}
	
	/**
	 * Invalidates the cache such that any future access to the value will re-create the
	 * value using the valueSupplier this cache was created with
	 */
	fun invalidate() {
		this.initialized = false;
		this.internalValue = null;
	}
	
	/**
	 * Initializes the value using the valueSupplier if it has not already been initialized
	 */
	fun poke() {
		if (!this.initialized) {
			this.internalValue = this.valueSupplier();
			this.initialized = true;
		}
	}
}