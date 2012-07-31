package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.AbstractElectroCraftMod;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.IComputer;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.IComputerRunnable;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockComputer extends BlockNetwork implements IComputerCallback {

	public BlockComputer(int id) {
		super(id, 40, Material.iron);
	}
	
	@Override
	public int getBlockTextureFromSide(int side) {
		return ElectroBlocks.COMPUTER.getDefaultTextureIndices()[side];
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityComputer();
	}
    
	@Override
	public boolean blockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer) {
		if (super.blockActivated(par1World, par2, par3, par4, par5EntityPlayer)) {
			return true;
		}
		
		if (par1World.getBlockTileEntity(par2, par3, par4) instanceof TileEntityComputer) {
			TileEntityComputer computerTileEntity = (TileEntityComputer)par1World.getBlockTileEntity(par2, par3, par4);
			if (computerTileEntity != null) {
				if (computerTileEntity.getComputer() == null)
					AbstractElectroCraftMod.getInstance().getComputerHandler().createAndStartCompuer(computerTileEntity, this);
				else
					this.onTaskComplete(computerTileEntity.getComputer());
		        return true;
			}
		}
		return false;
    }

	@Override
	public void onTaskComplete(Object object) {
		if (object instanceof IComputer) {
			if (!AbstractElectroCraftMod.getInstance().getComputerHandler().isComputerRunning((IComputer) object)) {
				AbstractElectroCraftMod.getInstance().getComputerHandler().startComputer((IComputer) object, this);
			}
			
			AbstractElectroCraftMod.getInstance().getComputerHandler().displayComputerGUI((IComputer)object);
		}
	}
}
