package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.AbstractElectroCraftMod;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public abstract class BlockNetwork extends ElectroBlock {

	protected BlockNetwork(int id, int textureId, Material material) {
		super(id, textureId, material);
	}
	
	protected BlockNetwork(int id, Material material) {
		super(id, material);
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).update((NetworkBlock) world.getBlockTileEntity(x, y, z));
        }
	}
	
	@Override
	public void onBlockRemoval(World world, int x, int y, int z) {
        super.onBlockRemoval(world, x, y, z);
        if (world.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
    		((NetworkBlock)world.getBlockTileEntity(x, y, z)).update((NetworkBlock) world.getBlockTileEntity(x, y, z));
        }
	}
	
	@Override
	public boolean blockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
		if (AbstractElectroCraftMod.getInstance().isShiftHeld()) {
			if (par1World.getBlockId(par2, par3, par4) == this.blockID) {
				if (par1World.getBlockTileEntity(par2, par3, par4) instanceof NetworkBlock) {
					AbstractElectroCraftMod.getInstance().getComputerHandler().displayNetworkGuiScreen((NetworkBlock) par1World.getBlockTileEntity(par2, par3, par4), par5EntityPlayer);
					return true;
				}
			}
		}
		return false;
	}
}
