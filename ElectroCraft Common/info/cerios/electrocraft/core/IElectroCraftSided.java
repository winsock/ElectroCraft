package info.cerios.electrocraft.core;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;

public interface IElectroCraftSided {
	
	public void init();
	
    public TileEntity getBlockTileEntity(int x, int y, int z, int d);

    public void closeGui(Object... optionalPlayers);
    
    public void openComputerGui(TileEntityComputer computer);
    
    public void openNetworkGui(NetworkBlock block);
    
    public IScheduledTickHandler getTickHandler();
    
    public IPacketHandler getPacketHandler();
    
    //					   //
    // Client Only Methods //
    //					   //
    public void loadTextures();
    
    public void registerRenderers();
    
    public int getFreeRenderId();
    
    public boolean isShiftHeld();
}
