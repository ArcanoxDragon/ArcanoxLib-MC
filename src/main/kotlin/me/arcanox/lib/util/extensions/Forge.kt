package me.arcanox.lib.util.extensions

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.IEventBus

// region Event Bus Stuff

/**
 * This method works around the issue of Forge not being able to figure out the Consumer type
 * when using a Kotlin class's methods as event listeners
 */
inline fun <reified T : Event> IEventBus.addKtListener(noinline listener: (T) -> Unit) =
	this.addListener(EventPriority.NORMAL, false, T::class.java, listener)

// endregion Event Bus Stuff