package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;

public enum ElectroItems {
	// Defaults
	WIRE(false, "wire", new Wire(ElectroBlocks.WIRE.getBlock().blockID - 256), "Tin Wire", "Copper Wire", "Gold Wire", "Redstone Wire"),
	COPPOR_INGOT(true, "ingotCopper", new CopperIngot(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("ingotCopper", "item", 30078).getInt(30078)).setIconIndex(1).setItemName("ingotCopper"), "Copper Ingot"),
	RUBBER(true, "rubber", new Rubber(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("rubber", "item", 30079).getInt(30079)).setIconIndex(2).setItemName("rubber"), "Rubber"),
	MAGNETITE_DUST(true, "magnetiteDust", new MagnetiteDust(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("magnetiteDust", "item", 30080).getInt(30080)).setIconIndex(3).setItemName("magnetiteDust"), "Magnetite Dust"),
	ELECTRO_DUST(false, "electroDust", new ElectroDust(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("electroDust", "item", 30081).getInt(30081)).setIconIndex(4).setItemName("electroDust"), "Electrodust");
	
	private boolean isOreDicItem;
	private String name;
	private String[] humanNames;
	private Item item;
	
	private ElectroItems(boolean isOreDicItem, String itemName, Item item, String... humanNames) {
		this.isOreDicItem = isOreDicItem;
		this.name = itemName;
		this.humanNames = humanNames;
		this.item = item;
		
		// Save the config in case any settings were written from the enum initialization
		ConfigHandler.getCurrentConfig().save();
	}
	
	public String getItemName() {
		return name;
	}
	
	public String[] getHumanNames() {
		return humanNames;
	}
	
	public boolean isOreDicItem() {
		return isOreDicItem;
	}
	
	public Item getItem() {
		return item;
	}
}
