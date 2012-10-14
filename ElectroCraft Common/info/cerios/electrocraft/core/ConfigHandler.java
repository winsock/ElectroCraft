package info.cerios.electrocraft.core;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.src.ModLoader;
import net.minecraftforge.common.Configuration;

import java.io.File;
import java.io.IOException;

public class ConfigHandler {

    private static File modConfigFolder;
    private static File currentConfigFile;
    private static Configuration currentConfig;

    public static Configuration getCurrentConfig() {
    	if (currentConfig == null) {
    		loadOrCreateConfigFile("default.cfg");
    	}
        return currentConfig;
    }

    public static void loadOrCreateConfigFile(String file) {
    	modConfigFolder = new File(ElectroCraft.electroCraftSided.getBaseDir(), "config" + File.separator + "ElectroCraft");
        if (!modConfigFolder.exists())
            modConfigFolder.mkdirs();
        currentConfigFile = new File(modConfigFolder + File.separator + file);
        if (!currentConfigFile.exists()) {
            try {
                currentConfigFile.createNewFile();
            } catch (IOException e) {
                ElectroCraft.instance.getLogger().severe("ElectroCraft: Unable to make config file! Do you have write permissions?");
            }
        }
        currentConfig = new Configuration(currentConfigFile);
        currentConfig.load();
    }
}
