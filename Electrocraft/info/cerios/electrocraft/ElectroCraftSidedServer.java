package info.cerios.electrocraft;

import cpw.mods.fml.common.FMLCommonHandler;
import info.cerios.electrocraft.core.IElectroCraftSided;
import info.cerios.electrocraft.core.network.ConnectionHandler;
import info.cerios.electrocraft.core.network.CustomPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;

import java.io.File;
import java.net.SocketAddress;

import cpw.mods.fml.server.FMLServerHandler;

public class ElectroCraftSidedServer implements IElectroCraftSided {

    private boolean isShiftDown = false;

    @Override
    public void init() {
        FMLCommonHandler.instance().bus().register(new ConnectionHandler());
    }

    @Override
    public void closeGui(Object... optionalPlayers) {
    }

    @Override
    public void openComputerGui() {
    }

    @Override
    public void openNetworkGui(NetworkAddressPacket packet) {
    }

    @Override
    public Object getTickHandler() {
        return null;
    }

    @Override
    public void registerRenderers() {
    }

    @Override
    public int getFreeRenderId() {
        return 0;
    }

    @Override
    public boolean isShiftHeld() {
        return isShiftDown;
    }

    public void setShiftState(boolean state) {
        this.isShiftDown = state;
    }

    @Override
    public void startComputerClient(int port, SocketAddress address) {
    }

    @Override
    public void handleClientCustomPacket(CustomPacket packet) {
    }

    @Override
    public Object getClientGuiFor(Gui gui, Object... args) {
        return null;
    }
}
