package info.cerios.electrocraft.core;

import java.io.File;
import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.src.ModLoader;
import net.minecraft.src.forge.Configuration;

public class ConfigHandler {
	
	private static File modConfigFolder;
	private static File currentConfigFile;
	private static Configuration currentConfig;
	
	public static Configuration getCurrentConfig() {
		return currentConfig;
	}
	
	public static void loadOrCreateConfigFile(String file) {
		currentConfigFile = new File(modConfigFolder + File.separator + file);
		if (!currentConfigFile.exists()) {
			try {
				currentConfigFile.createNewFile();
			} catch (IOException e) {
				ModLoader.getLogger().severe("ElectroCraft: Unable to make config file! Do you have write permissions?");
			}
		}
		currentConfig = new Configuration(currentConfigFile);
		currentConfig.load();
	}
	
	static {
		modConfigFolder = new File(FMLCommonHandler.instance().getMinecraftRootDirectory() + File.separator + "config" + File.separator + "ElectroCraft");
		if (!modConfigFolder.exists())
			modConfigFolder.mkdirs();
	}
}