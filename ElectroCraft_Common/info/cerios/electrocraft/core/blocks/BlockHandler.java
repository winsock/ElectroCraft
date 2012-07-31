package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.items.ItemHandler;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.forge.IOreHandler;
import net.minecraft.src.forge.oredict.OreDictionary;

public class BlockHandler implements IOreHandler {

	private ItemHandler itemHandler;
	
	public BlockHandler(ItemHandler itemHandler) {
		this.itemHandler = itemHandler;
	}
	
	public void registerBlocks() {
		for (ElectroBlocks block : ElectroBlocks.values()) {
			if (block.getBlockItem() != null) {
				ModLoader.registerBlock(block.getBlock(), block.getBlockItem());
			} else {
				ModLoader.registerBlock(block.getBlock());
			}
			if (block.getTileEntity() != null) {
				ModLoader.registerTileEntity(block.getTileEntity(), block.getName());
			}
			ModLoader.addName(block.getBlock(), block.getHumanName());
			if (block.isOreDicBlock())
				OreDictionary.registerOre(block.getName(), block.getBlock());
		}
	}
	
	@Override
	public void registerOre(String oreClass, ItemStack ore) {
		itemHandler.registerOreDicItem(oreClass, ore.getItem());
	}
}
