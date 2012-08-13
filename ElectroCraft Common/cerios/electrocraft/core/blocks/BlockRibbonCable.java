package info.cerios.electrocraft.core.blocks;

import java.util.ArrayList;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRibbonCable;
import net.minecraft.src.*;
import net.minecraftforge.common.Orientation;

public class BlockRibbonCable extends BlockNetwork {

    public static int renderId = ElectroCraft.electroCraftSided.getFreeRenderId();

    public BlockRibbonCable(int id) {
        super(id, 10, Material.circuits);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
    }

    @Override
    public int getBlockTextureFromSide(int side) {
        return ElectroBlocks.RIBBON_CABLE.getDefaultTextureIndices()[side];
    }
    
    @Override
	public TileEntity createNewTileEntity(World var1) {
        return new TileEntityRibbonCable();
	}
    
    @Override
    public void addCreativeItems(ArrayList itemList) { 
    	itemList.add(this);
    }

    // ============ BEGIN COPIED STUFF FROM BlockRedstoneWire ================= //

    @Override
    public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
        return par1World.isBlockSolidOnSide(par2, par3 - 1, par4, Orientation.UP) || par1World.getBlockId(par2, par3 - 1, par4) == Block.glowStone.blockID;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return renderId;
    }
}
