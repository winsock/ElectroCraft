package info.cerios.electrocraft.blocks.render;

import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.blocks.IBlockRenderer;

public enum BlockRenderers {
    RIBBON_CABLE(new RibbonCableRenderer(), ElectroBlocks.RIBBON_CABLE);

    private IBlockRenderer renderer;
    private ElectroBlocks block;

    private BlockRenderers(IBlockRenderer renderer, ElectroBlocks block) {
        this.renderer = renderer;
        this.block = block;
    }

    public ElectroBlocks getBlock() {
        return block;
    }

    public IBlockRenderer getRenderer() {
        return renderer;
    }
}
