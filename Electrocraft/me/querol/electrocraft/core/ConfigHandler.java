package me.querol.electrocraft.core;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

    private static Configuration currentConfig;

    public static Configuration getCurrentConfig() {
        currentConfig.load();
        return currentConfig;
    }

    public static void loadOrCreateConfigFile(File config) {
        currentConfig = new Configuration(config);
        currentConfig.load();
        currentConfig.save();
    }
}
