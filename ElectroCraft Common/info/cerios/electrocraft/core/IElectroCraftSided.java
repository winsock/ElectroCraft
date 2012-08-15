package info.cerios.electrocraft.core;

import java.net.SocketAddress;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;

public interface IElectroCraftSided {
	
	public void init();
	
    public TileEntity getBlockTileEntity(int x, int y, int z, int d);

    public void closeGui(Object... optionalPlayers);
    
    public void openComputerGui();
    
    public void openNetworkGui(NetworkAddressPacket packet);
    
    public IScheduledTickHandler getTickHandler();
        
    //					   //
    // Client Only Methods //
    //					   //
    public void loadTextures();
    
    public void registerRenderers();
    
    public int getFreeRenderId();
    
    public boolean isShiftHeld();
    
    public void startComputerClient(int port, SocketAddress address);
}
