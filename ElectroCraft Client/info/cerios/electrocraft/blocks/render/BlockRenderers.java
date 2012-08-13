package info.cerios.electrocraft.blocks.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;

public enum BlockRenderers {
    RIBBON_CABLE(new RibbonCableRenderer(), ElectroBlocks.RIBBON_CABLE);

    private ISimpleBlockRenderingHandler renderer;
    private ElectroBlocks block;

    private BlockRenderers(ISimpleBlockRenderingHandler renderer, ElectroBlocks block) {
        this.renderer = renderer;
        this.block = block;
    }

    public ElectroBlocks getBlock() {
        return block;
    }

    public ISimpleBlockRenderingHandler getRenderer() {
        return renderer;
    }
}
