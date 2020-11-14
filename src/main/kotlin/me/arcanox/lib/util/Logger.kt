package me.arcanox.lib.util

import me.arcanox.lib.ArcanoxLibMod
import me.arcanox.lib.common.IInitHandler
import me.arcanox.lib.util.reflect.InitHandler
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.MarkerManager

@InitHandler(priority = 0 /* before everything */)
object Logger : IInitHandler {
	val marker = MarkerManager.getMarker(ArcanoxLibMod.ModID.toUpperCase())!!;
	var logger: Logger? = null;
	
	override fun onInit(e: FMLCommonSetupEvent) {
		this.logger = LogManager.getLogger();
	}
	
	internal fun format(message: Any) = "[${ArcanoxLibMod.Name}] $message";
	
	private val safeLogger: Logger = logger ?: LogManager.getLogger()
	
	fun debug(message: Any) = safeLogger.debug(marker, this.format(message));
	fun info(message: Any) = safeLogger.info(marker, this.format(message));
	fun warn(message: Any) = safeLogger.warn(marker, this.format(message));
	fun error(message: Any) = safeLogger.error(marker, this.format(message));
	fun fatal(message: Any) = safeLogger.fatal(marker, this.format(message));
}

