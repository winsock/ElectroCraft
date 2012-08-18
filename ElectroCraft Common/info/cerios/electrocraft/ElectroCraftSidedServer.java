package info.cerios.electrocraft;

import java.net.SocketAddress;

import net.minecraft.src.TileEntity;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import info.cerios.electrocraft.core.IElectroCraftSided;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;

public class ElectroCraftSidedServer implements IElectroCraftSided {

	private boolean isShiftDown = false;
	
	@Override
	public void init() { }

	@Override
	public TileEntity getBlockTileEntity(int x, int y, int z, int d) { return null; }

	@Override
	public void closeGui(Object... optionalPlayers) { }

	@Override
	public void openComputerGui() { }

	@Override
	public void openNetworkGui(NetworkAddressPacket packet) { }

	@Override
	public IScheduledTickHandler getTickHandler() { return null; }

	@Override
	public void loadTextures() { }

	@Override
	public void registerRenderers() { }

	@Override
	public int getFreeRenderId() { return 0; }

	@Override
	public boolean isShiftHeld() {
		return isShiftDown;
	}
	
	public void setShiftState(boolean state) {
		this.isShiftDown = state;
	}

	@Override
	public void startComputerClient(int port, SocketAddress address) { }
}
