package info.cerios.electrocraft.core.blocks;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRedstoneAdapter;

public class BlockRedstoneAdapter extends BlockNetwork {
	public BlockRedstoneAdapter(int id) {
		super(id, 117, Material.rock);
		this.setBlockBounds(0f, 0f, 0f, 1.0f, 0.55f, 1.0f);
	}
	
	@Override
	public boolean isIndirectlyPoweringTo(World par1World, int par2, int par3, int par4, int par5) {
		 return isPoweringTo(par1World, par2, par3, par4, par5);
	}
	
	@Override
    public boolean isPoweringTo(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        if (par1IBlockAccess.getBlockTileEntity(par2, par3, par4) instanceof TileEntityRedstoneAdapter) {
        	TileEntityRedstoneAdapter adapter = (TileEntityRedstoneAdapter) par1IBlockAccess.getBlockTileEntity(par2, par3, par4);
        	return adapter.getRedstonePower();
        }
        return false;
    }
	
	@Override
	public int getBlockTextureFromSide(int side) {
		return ElectroBlocks.REDSTONE_ADAPTER.getDefaultTextureIndices()[side];
	}
	
	// Needed to allow block to emit a redstone signal
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
		return 0;
	}
	
    @Override
    public boolean canProvidePower() {
        return true;
    }
    
	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityRedstoneAdapter();
	}
}
