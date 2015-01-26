package me.querol.electrocraft.core.blocks;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import me.querol.electrocraft.core.items.ItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.LanguageRegistry;

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
            LanguageRegistry.addName(block.getBlock(), block.getHumanName()); // TODO: Localization
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
