package info.cerios.electrocraft.core.blocks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import info.cerios.electrocraft.core.items.ItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class BlockHandler {

    private ItemHandler itemHandler;

    public BlockHandler(ItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    public void registerBlocks() {
        for (ElectroBlocks block : ElectroBlocks.values()) {
            if (block.getBlockItem() != null) {
                GameRegistry.registerBlock(block.getBlock(), block.getBlockItem(), block.getName());

            } else {
                GameRegistry.registerBlock(block.getBlock(), block.getName());
            }
            if (block.getTileEntity() != null) {
                GameRegistry.registerTileEntity(block.getTileEntity(), block.getName());
            }
            LanguageRegistry.addName(block.getBlock(), block.getHumanName());
            if (block.isOreDicBlock()) {
                OreDictionary.registerOre(block.getName(), block.getBlock());
            }
        }
    }

    @SubscribeEvent
    public void invoke(OreRegisterEvent event) {
        itemHandler.registerOreDicItem(event.Name, event.Ore.getItem());
    }
}
