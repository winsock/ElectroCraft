package info.cerios.electrocraft.core.items;

import info.cerios.electrocraft.core.ConfigHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public enum ElectroItems {
    // Defaults
    MAGNETITE_DUST(true, "magnetiteDust", new MagnetiteDust().setUnlocalizedName("magnetiteDust").setCreativeTab(CreativeTabs.tabMaterials), "Magnetite Dust"), ELECTRO_DUST(false, "electroDust", new ElectroDust().setUnlocalizedName("electroDust").setCreativeTab(CreativeTabs.tabRedstone), "Electrodust"), DRONE_UPGRADES(false, "droneUpgrades", new ItemDroneUpgrade().setUnlocalizedName("droneUpgrades").setCreativeTab(CreativeTabs.tabRedstone), "Drone Gyroscope Card", "Drone GPS Card", "Drone Analyzer Card", "Drone Engine Card", "Drone AI Card"), DRONE(false, "itemDrone", new ItemDrone().setUnlocalizedName("itemDrone").setCreativeTab(CreativeTabs.tabRedstone), "Drone");

    private boolean isOreDicItem;
    private String name;
    private String[] humanNames;
    private Item item;

    private ElectroItems(boolean isOreDicItem, String itemName, Item item, String... humanNames) {
        this.isOreDicItem = isOreDicItem;
        this.name = itemName;
        this.humanNames = humanNames;
        this.item = item;

        // Save the config in case any settings were written from the enum
        // initialization
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
