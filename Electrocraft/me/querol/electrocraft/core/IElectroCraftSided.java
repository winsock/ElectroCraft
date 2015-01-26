package me.querol.electrocraft.core;

import me.querol.electrocraft.core.network.CustomPacket;
import me.querol.electrocraft.core.network.GuiPacket.Gui;
import me.querol.electrocraft.core.network.NetworkAddressPacket;

import java.io.File;
import java.net.SocketAddress;

public interface IElectroCraftSided {

    public void init();

    public void closeGui(Object... optionalPlayers);

    public void openComputerGui();

    public void openNetworkGui(NetworkAddressPacket packet);

    public Object getTickHandler();

    // //
    // Client Only Methods //
    // //

    public void registerRenderers();

    public boolean isShiftHeld();

    public void startComputerClient(int port, SocketAddress address);

    public void handleClientCustomPacket(CustomPacket packet);

    public Object getClientGuiFor(Gui gui, Object... args);
}
