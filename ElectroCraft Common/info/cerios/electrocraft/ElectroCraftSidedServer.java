package info.cerios.electrocraft;

import java.io.File;
import java.net.SocketAddress;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.src.TileEntity;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.server.FMLServerHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.IElectroCraftSided;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.IComputerRunnable;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.network.CustomPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;

public class ElectroCraftSidedServer implements IElectroCraftSided {

	private boolean isShiftDown = false;
	
	@Override
	public void init() { }

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
	public File getBaseDir() {
		return FMLServerHandler.instance().getServer().getFile(".");
	}
	
	@Override
	public void startComputerClient(int port, SocketAddress address) { }

	@Override
	public void handleClientCustomPacket(CustomPacket packet) { }

	@Override
	public Object getClientGuiFor(Gui gui, Object... args) { return null; }
}
