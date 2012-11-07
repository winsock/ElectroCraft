package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRedstoneAdapter;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.TileEntity;

public enum ElectroBlocks {
	// TODO XXX FIXME Remember to credit billythegoat101 for textures of the
	// blocks
	MAGNETITE_ORE(true, "magnetiteOre", "Magnetite Ore", new MagnetiteOre(
			ConfigHandler.getCurrentConfig().getBlock("magnetiteOre", 3976)
					.getInt(3976)).setBlockName("magnetiteOre").setCreativeTab(
			CreativeTabs.tabMaterials), null, null, 4), COMPUTER(true,
			"xcomputer", "Computer", new BlockComputer(ConfigHandler
					.getCurrentConfig().getBlock("xcomputer", 3977)
					.getInt(3977)).setBlockName("xcomputer").setCreativeTab(
					CreativeTabs.tabRedstone), null, TileEntityComputer.class,
			32, 37), REDSTONE_ADAPTER(true, "redstoneAdapter",
			"Redstone Adapter", new BlockRedstoneAdapter(ConfigHandler
					.getCurrentConfig().getBlock("redstoneAdapter", 3980)
					.getInt(3980)).setBlockName("redstoneAdapter")
					.setCreativeTab(CreativeTabs.tabRedstone), null,
			TileEntityRedstoneAdapter.class, 149, 148, 147, 147, 147, 147), SERIAL_CABLE(
			false, "serialcable", "Serial Cable", new BlockSerialCable(
					ConfigHandler.getCurrentConfig()
							.getBlock("serialCable", 3981).getInt(3981))
					.setBlockName("serialCable").setCreativeTab(
							CreativeTabs.tabRedstone), null,
			TileEntitySerialCable.class, 0);

	private boolean isOreDicBlock;
	private String name, humanName;
	private Class<? extends ItemBlock> blockItem;
	private Class<? extends TileEntity> tileEntity;
	private Block block;
	private int[] defaultTextureIndices;

	private ElectroBlocks(boolean isOreDicBlock, String name, String humanName,
			Block block, Class<? extends ItemBlock> blockItem,
			Class<? extends TileEntity> tileEntity,
			int... defaultTextureIndices) {
		this.isOreDicBlock = isOreDicBlock;
		this.name = name;
		this.humanName = humanName;
		this.block = block;
		this.blockItem = blockItem;
		this.defaultTextureIndices = defaultTextureIndices;
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
