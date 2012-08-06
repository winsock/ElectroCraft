package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRedstoneAdapter;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRibbonCable;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;

public enum ElectroBlocks {
	// TODO XXX FIXME Remember to credit billythegoat101 for textures of the blocks
	
	COPPER_ORE(true,
			"copperOre", "Copper Ore",
			new CopperOre(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("copperOre", 3974).getInt(3974)).setBlockName("copperOre"),
			null, null,
			3),
	MAGNETITE_ORE(true,
			"magnetiteOre", "Magnetite Ore",
			new MagnetiteOre(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("magnetiteOre", 3976).getInt(3976)).setBlockName("magnetiteOre"),
			null, null,
			4),
	COMPUTER(true,
			"xcomputer", "Computer",
			new BlockComputer(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("xcomputer", 3979).getInt(3979)).setBlockName("xcomputer"),
			null, TileEntityComputer.class,
			37, 36, 34, 32, 35, 33),
	REDSTONE_ADAPTER(true,
			"redstoneAdapter", "Redstone Adapter",
			new BlockRedstoneAdapter(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("redstoneAdapter", 3980).getInt(3980)).setBlockName("redstoneAdapter"),
			null, TileEntityRedstoneAdapter.class,
			149, 148, 147, 147, 147, 147),
	RIBBON_CABLE(false,
			"ribbonCable", "Ribbon Cable",
			new BlockRibbonCable(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("ribbonCable", 3981).getInt(3981)).setBlockName("ribbonCable"),
			null, TileEntityRibbonCable.class,
			74, 74, 75, 91, 90);
	
	private boolean isOreDicBlock;
	private String name, humanName;
	private Class<? extends ItemBlock> blockItem;
	private Class<? extends TileEntity> tileEntity;
	private Block block;
	private int[] defaultTextureIndices;
	
	private ElectroBlocks(boolean isOreDicBlock, String name, String humanName, Block block, Class<? extends ItemBlock> blockItem, Class<? extends TileEntity> tileEntity, int... defaultTextureIndices) {
		this.isOreDicBlock = isOreDicBlock;
		this.name = name;
		this.humanName = humanName;
		this.block = block;
		this.blockItem = blockItem;
		this.defaultTextureIndices = defaultTextureIndices;
		this.tileEntity = tileEntity;
		
		// Save the config in case any settings were written from the enum initialization
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
	
	public int[] getDefaultTextureIndices() {
		return defaultTextureIndices;
	}
	
	public Class<? extends ItemBlock> getBlockItem() {
		return blockItem;
	}
	
	public Class<? extends TileEntity> getTileEntity() {
		return tileEntity;
	}
}
