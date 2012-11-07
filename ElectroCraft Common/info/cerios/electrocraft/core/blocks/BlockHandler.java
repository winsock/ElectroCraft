package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.items.ItemHandler;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.IEventListener;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class BlockHandler implements IEventListener {

	private ItemHandler itemHandler;

	public BlockHandler(ItemHandler itemHandler) {
		this.itemHandler = itemHandler;
	}

	public void registerBlocks() {
		for (ElectroBlocks block : ElectroBlocks.values()) {
			if (block.getBlockItem() != null) {
				GameRegistry.registerBlock(block.getBlock(),
						block.getBlockItem());
			} else {
				GameRegistry.registerBlock(block.getBlock());
			}
			if (block.getTileEntity() != null) {
				GameRegistry.registerTileEntity(block.getTileEntity(),
						block.getName());
			}
			LanguageRegistry.addName(block.getBlock(), block.getHumanName());
			if (block.isOreDicBlock()) {
				OreDictionary.registerOre(block.getName(), block.getBlock());
			}
		}
	}

	@Override
	public void invoke(Event event) {
		if (event.getClass().isAssignableFrom(
				OreDictionary.OreRegisterEvent.class)) {
			OreRegisterEvent oreEvent = (OreRegisterEvent) event;
			itemHandler.registerOreDicItem(oreEvent.Name,
					oreEvent.Ore.getItem());
		}
	}
}
