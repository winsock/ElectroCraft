package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;

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
