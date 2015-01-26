package me.querol.electrocraft;

import me.querol.electrocraft.core.IElectroCraftSided;
import me.querol.electrocraft.core.network.ConnectionHandler;
import me.querol.electrocraft.core.network.CustomPacket;
import me.querol.electrocraft.core.network.GuiPacket;
import me.querol.electrocraft.core.network.NetworkAddressPacket;
import net.minecraftforge.fml.common.FMLCommonHandler;
import me.querol.electrocraft.core.IElectroCraftSided;
import me.querol.electrocraft.core.network.ConnectionHandler;
import me.querol.electrocraft.core.network.CustomPacket;
import me.querol.electrocraft.core.network.GuiPacket.Gui;
import me.querol.electrocraft.core.network.NetworkAddressPacket;

import java.io.File;
import java.net.SocketAddress;

import net.minecraftforge.fml.server.FMLServerHandler;

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
    public Object getClientGuiFor(GuiPacket.Gui gui, Object... args) {
        return null;
    }
}
