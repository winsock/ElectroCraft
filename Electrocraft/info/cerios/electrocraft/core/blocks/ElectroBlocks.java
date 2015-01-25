package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRedstoneAdapter;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;

public enum ElectroBlocks {
    // TODO Remember to credit billythegoat101 for textures of the blocks
    MAGNETITE_ORE(true, "magnetiteOre", "Magnetite Ore", new MagnetiteOre().setUnlocalizedName("magnetiteOre").setCreativeTab(CreativeTabs.tabMaterials), null, null), COMPUTER(true, "computer", "Computer", new BlockComputer().setUnlocalizedName("computer").setCreativeTab(CreativeTabs.tabRedstone), null, TileEntityComputer.class), REDSTONE_ADAPTER(true, "redstoneAdapter", "Redstone Adapter", new BlockRedstoneAdapter().setUnlocalizedName("redstoneAdapter").setCreativeTab(CreativeTabs.tabRedstone), null, TileEntityRedstoneAdapter.class), SERIAL_CABLE(false, "serialcable", "Serial Cable", new BlockSerialCable().setUnlocalizedName("serialCable").setCreativeTab(CreativeTabs.tabRedstone), null, TileEntitySerialCable.class);

    private boolean isOreDicBlock;
    private String name, humanName;
    private Class<? extends ItemBlock> blockItem;
    private Class<? extends TileEntity> tileEntity;
    private Block block;

    private ElectroBlocks(boolean isOreDicBlock, String name, String humanName, Block block, Class<? extends ItemBlock> blockItem, Class<? extends TileEntity> tileEntity) {
        this.isOreDicBlock = isOreDicBlock;
        this.name = name;
        this.humanName = humanName;
        this.block = block;
        this.blockItem = blockItem;
        this.tileEntity = tileEntity;

        // Save the config in case any settings were written from the enum
        // initialization
        ConfigHandler.getCurrentConfig().save();
    }

    public Block getBlock() {
        return block;
    }

    public boolean isOreDicBlock() {
        return isOreDicBlock;
    }

    public String getName() {
        return name;
    }

    public String getHumanName() {
        return humanName;
    }

    public Class<? extends ItemBlock> getBlockItem() {
        return blockItem;
    }

    public Class<? extends TileEntity> getTileEntity() {
        return tileEntity;
    }
}
