package info.cerios.electrocraft.core.blocks;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRibbonCable;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.electricity.ElectricBlock;

public class BlockRibbonCable extends ElectroBlock {

	public static int renderId = ModLoader.getUniqueBlockModelID(mod_ElectroCraft.instance, false);
	
	public BlockRibbonCable(int id) {
		super(id, 10, Material.circuits);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityRibbonCable();
	}
	
	@Override
	public int getBlockTextureFromSide(int side) {
		return ElectroBlocks.RIBBON_CABLE.getDefaultTextureIndices()[side];
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).updateComputerNetwork();
        }
	}
	
	@Override
	public void onBlockRemoval(World world, int x, int y, int z) {
        super.onBlockRemoval(world, x, y, z);
        if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).updateComputerNetwork();
        }
	}

	// ============ BEGIN COPIED STUFF FROM BlockRedstoneWire ================= //

	@Override
	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
        return par1World.isBlockSolidOnSide(par2, par3 - 1, par4, 1) || par1World.getBlockId(par2, par3 - 1, par4) == Block.glowStone.blockID;
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
