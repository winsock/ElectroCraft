package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.ComputerHandler;
import info.cerios.electrocraft.gui.GuiComputerScreen;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class BlockComputer extends ElectroBlock {

	public BlockComputer(int id) {
		super(id, 11, Material.iron);
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityComputer(mod_ElectroCraft.instance.getComputerHandler());
	}
	
    public boolean isIndirectlyPoweringTo(World par1World, int par2, int par3, int par4, int par5) {
    	return true;
    }
    
	public boolean blockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer)
    {
		if (par1World.getBlockTileEntity(par2, par3, par4) instanceof TileEntityComputer) {
			TileEntityComputer computerTileEntity = (TileEntityComputer)par1World.getBlockTileEntity(par2, par3, par4);
			if (computerTileEntity != null) {
				if (!mod_ElectroCraft.instance.getComputerHandler().isComputerRunning(computerTileEntity.getComputer())) {
					mod_ElectroCraft.instance.getComputerHandler().startComputer(computerTileEntity.getComputer());
				}
				ModLoader.getMinecraftInstance().displayGuiScreen(new GuiComputerScreen(computerTileEntity.getComputer()));
		        return true;
			}
		}
		return false;
    }
}
