package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.core.ConfigHandler;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;

public enum ElectroItems {
    // Defaults
    MAGNETITE_DUST(true, "magnetiteDust", new MagnetiteDust(ConfigHandler.getCurrentConfig().get("item", "magnetiteDust", 30080).getInt(30080)).setIconIndex(3).setItemName("magnetiteDust").setCreativeTab(CreativeTabs.tabMaterials), "Magnetite Dust"),
    ELECTRO_DUST(false, "electroDust", new ElectroDust(ConfigHandler.getCurrentConfig().get("item", "electroDust", 30081).getInt(30081)).setIconIndex(4).setItemName("electroDust").setCreativeTab(CreativeTabs.tabRedstone), "Electrodust"),
    DRONE_UPGRADES(false, "droneUpgrades", new ItemDroneUpgrade(ConfigHandler.getCurrentConfig().get("item", "droneUpgrades", 30082).getInt(30082)).setIconIndex(5).setItemName("droneUpgrades").setCreativeTab(CreativeTabs.tabRedstone), "Drone Gyroscope Card", "Drone GPS Card", "Drone Analyzer Card", "Drone Engine Card", "Drone AI Card");
    
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
