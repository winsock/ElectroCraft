package info.cerios.electrocraft.core;

import info.cerios.electrocraft.core.network.CustomPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;

import java.io.File;
import java.net.SocketAddress;

import cpw.mods.fml.common.IScheduledTickHandler;

public interface IElectroCraftSided {

	public void init();

	public void closeGui(Object... optionalPlayers);

	public void openComputerGui();

	public void openNetworkGui(NetworkAddressPacket packet);

	public IScheduledTickHandler getTickHandler();

	public File getBaseDir();

	// //
	// Client Only Methods //
	// //
	public void loadTextures();

	public void registerRenderers();

	public int getFreeRenderId();

	public boolean isShiftHeld();

	public void startComputerClient(int port, SocketAddress address);

	public void handleClientCustomPacket(CustomPacket packet);

	public Object getClientGuiFor(Gui gui, Object... args);
}
