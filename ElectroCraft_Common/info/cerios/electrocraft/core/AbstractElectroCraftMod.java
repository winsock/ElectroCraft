package info.cerios.electrocraft.core;

import cpw.mods.fml.common.modloader.BaseMod;
import net.minecraft.src.ModLoader;
import info.cerios.electrocraft.core.computer.IComputerHandler;

public abstract class AbstractElectroCraftMod implements IElectroCraftMod {

	private static IElectroCraftMod instance;
	
	public static IElectroCraftMod getInstance() {
		if (instance != null) {
			return instance;
		}
		for (BaseMod mod : ModLoader.getLoadedMods()) {
			if (IElectroCraftMod.class.isAssignableFrom(mod.getClass())) {
				instance = (IElectroCraftMod) mod;
				return (IElectroCraftMod) mod;
			}
		}
		ModLoader.getLogger().severe("ElectroCraft: Unable to find self in loaded mods list!");
		// We should NEVER get here
		return null;
	}
	
	// Make the constructor private
	private AbstractElectroCraftMod() {}
}
