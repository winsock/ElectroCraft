package me.querol.electrocraft.core.blocks;

import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.blocks.tileentities.TileEntitySerialCable;

import me.querol.electrocraft.core.blocks.tileentities.TileEntitySerialCable;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSerialCable extends BlockNetwork {
    public BlockSerialCable() {
        super(Material.cloth);
        this.setBlockBounds(0.20F, 0.20F, 0.20F, 0.70F, 0.70F, 0.70F);
    }
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_) {
        return new TileEntitySerialCable();
    }
}
