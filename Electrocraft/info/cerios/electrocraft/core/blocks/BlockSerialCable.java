package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSerialCable extends BlockNetwork {

    public static final int renderId = ElectroCraft.electroCraftSided.getFreeRenderId();

    public BlockSerialCable(int id) {
        super(id, 0, Material.cloth);
        this.setBlockBounds(0.20F, 0.20F, 0.20F, 0.70F, 0.70F, 0.70F);
    }

    @Override
    public TileEntity createNewTileEntity(World var1) {
        return new TileEntitySerialCable();
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public void addCreativeItems(ArrayList itemList) {
        itemList.add(this);
    }
}
