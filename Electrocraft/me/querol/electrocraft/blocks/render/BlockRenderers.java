package me.querol.electrocraft.blocks.render;

import me.querol.electrocraft.core.blocks.ElectroBlocks;
import me.querol.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import me.querol.electrocraft.core.blocks.ElectroBlocks;
import me.querol.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public enum BlockRenderers {
    SERIAL_CABLE(new SerialCableRenderer(), ElectroBlocks.SERIAL_CABLE, TileEntitySerialCable.class);

    private TileEntitySpecialRenderer specialRenderer;
    private ElectroBlocks block;
    private Class<? extends TileEntity> tileClass;

    private BlockRenderers(TileEntitySpecialRenderer specialRenderer, ElectroBlocks block, Class<? extends TileEntity> tileClass) {
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


    public TileEntitySpecialRenderer getSpecialRenderer() {
        return specialRenderer;
    }
}
