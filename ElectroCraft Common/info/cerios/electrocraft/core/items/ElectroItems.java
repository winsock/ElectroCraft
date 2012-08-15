package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.core.ConfigHandler;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;

public enum ElectroItems {
    // Defaults
    COPPOR_INGOT(true, "ingotCopper", new CopperIngot(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("ingotCopper", "item", 30078).getInt(30078)).setIconIndex(1).setItemName("ingotCopper").setTabToDisplayOn(CreativeTabs.tabMaterials), "Copper Ingot"),
    MAGNETITE_DUST(true, "magnetiteDust", new MagnetiteDust(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("magnetiteDust", "item", 30080).getInt(30080)).setIconIndex(3).setItemName("magnetiteDust").setTabToDisplayOn(CreativeTabs.tabMaterials), "Magnetite Dust"),
    ELECTRO_DUST(false, "electroDust", new ElectroDust(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("electroDust", "item", 30081).getInt(30081)).setIconIndex(4).setItemName("electroDust").setTabToDisplayOn(CreativeTabs.tabRedstone), "Electrodust");

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
