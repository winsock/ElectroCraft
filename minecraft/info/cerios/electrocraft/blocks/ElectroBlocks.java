package info.cerios.electrocraft.blocks;

import info.cerios.electrocraft.blocks.render.ElectroWireRenderer;
import info.cerios.electrocraft.blocks.render.IBlockRenderer;
import info.cerios.electrocraft.config.ConfigHandler;
import info.cerios.electrocraft.core.blocks.BlockComputer;
import info.cerios.electrocraft.core.blocks.CopperOre;
import info.cerios.electrocraft.core.blocks.ElectroWire;
import info.cerios.electrocraft.core.blocks.MagnetiteOre;
import info.cerios.electrocraft.core.blocks.StaticGenerator;
import info.cerios.electrocraft.core.blocks.Wire;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityElectroWire;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityStaticGenerator;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityWire;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.TileEntity;

public enum ElectroBlocks {
	COPPER_ORE(true,
			"copperOre", "Copper Ore",
			new CopperOre(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("copperOre", 3974).getInt(3974)).setBlockName("copperOre"),
			null, null, null,
			0),
	WIRE(false,
			"wire", "Wire",
			new Wire(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("wire", 3975).getInt(3975)).setBlockName("wire"),
			info.cerios.electrocraft.items.Wire.class, TileEntityWire.class, null,
			// 4 Different Blocks with texture id's as follows
			1, 2, 3, 4), // 1 = Tin, 2 = Copper, 3 = Gold, 4 = Redstone
	MAGNETITE_ORE(true,
			"magnetiteOre", "Magnetite Ore",
			new MagnetiteOre(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("magnetiteOre", 3976).getInt(3976)).setBlockName("magnetiteOre"),
			null, null, null,
			5),
	ELECTRO_WIRE(false,
			"electroWire", "Electro Wire",
			new ElectroWire(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("electroWire", 3977).getInt(3977)).setBlockName("electroWire"),
			null, TileEntityElectroWire.class, new ElectroWireRenderer(),
			6, 7, 8, 9),
	STATIC_GENERATOR(true,
			"staticGenerator", "Static Generator",
			new StaticGenerator(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("staticGenerator", 3978).getInt(3978)).setBlockName("staticGenerator"),
			null, TileEntityStaticGenerator.class, null,
			10),
	COMPUTER(true,
			"computer", "Computer",
			new BlockComputer(ConfigHandler.getCurrentConfig().getOrCreateBlockIdProperty("computer", 3979).getInt(3979)).setBlockName("computer"),
			null, TileEntityComputer.class, null,
			10);
	
	private boolean isOreDicBlock;
	private String name, humanName;
	private Class<? extends ItemBlock> blockItem;
	private Class<? extends TileEntity> tileEntity;
	private Block block;
	private IBlockRenderer renderer;
	private int[] defaultTextureIndices;
	
	private ElectroBlocks(boolean isOreDicBlock, String name, String humanName, Block block, Class<? extends ItemBlock> blockItem, Class<? extends TileEntity> tileEntity, IBlockRenderer renderer, int... defaultTextureIndices) {
		this.isOreDicBlock = isOreDicBlock;
		this.name = name;
		this.humanName = humanName;
		this.block = block;
		this.blockItem = blockItem;
		this.defaultTextureIndices = defaultTextureIndices;
		this.renderer = renderer;
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
	
	public boolean callRenderer(RenderBlocks renderBlocks, Block block, int x, int y, int z) {
		if (renderer == null)
			return false;
		return renderer.render(renderBlocks, block, x, y, z);
	}
}
