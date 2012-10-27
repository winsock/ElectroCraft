package info.cerios.electrocraft.blocks.render;

import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;

public enum BlockRenderers {
    SERIAL_CABLE(null, new SerialCableRenderer(), ElectroBlocks.SERIAL_CABLE, TileEntitySerialCable.class);

    private ISimpleBlockRenderingHandler renderer;
    private TileEntitySpecialRenderer specialRenderer;
    private ElectroBlocks block;
    private Class<? extends TileEntity> tileClass;
    
    private BlockRenderers(ISimpleBlockRenderingHandler renderer, TileEntitySpecialRenderer specialRenderer, ElectroBlocks block, Class<? extends TileEntity> tileClass) {
        this.renderer = renderer;
        this.specialRenderer = specialRenderer;
        this.block = block;
        this.tileClass = tileClass;
    }
    
    public Class<? extends TileEntity> getTileClass() {
    	return tileClass;
    }

    public ElectroBlocks getBlock() {
        return block;
    }

    public ISimpleBlockRenderingHandler getRenderer() {
        return renderer;
    }
    
    public TileEntitySpecialRenderer getSpecialRenderer() {
    	return specialRenderer;
    }
}
