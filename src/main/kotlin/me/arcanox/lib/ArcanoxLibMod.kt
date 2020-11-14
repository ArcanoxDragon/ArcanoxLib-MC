package me.arcanox.lib

import net.minecraftforge.fml.common.Mod

@Mod(ArcanoxLibMod.ModID)
class ArcanoxLibMod : ArcanoxModBase() {
	companion object {
		const val Name = "ArcanoxLib"
		const val ModID = "arcanoxlib"
		const val PackagePrefix = "me.arcanox.lib"
	}
	
	override val modId: String
		get() = ModID
	override val packagePrefix: String
		get() = PackagePrefix
	
	init {
		finalizeInit();
	}
}