package info.cerios.electrocraft;

import info.cerios.electrocraft.blocks.render.BlockRenderers;
import info.cerios.electrocraft.computer.ComputerClient;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.IElectroCraftSided;
import info.cerios.electrocraft.core.container.ContainerDrone;
import info.cerios.electrocraft.core.entites.EntityDrone;
import info.cerios.electrocraft.core.network.CustomPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.entites.ModelDrone;
import info.cerios.electrocraft.entites.RenderDrone;
import info.cerios.electrocraft.gui.GuiComputerScreen;
import info.cerios.electrocraft.gui.GuiDroneInventory;
import info.cerios.electrocraft.gui.GuiNetworkAddressScreen;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ElectroCraftClient implements IElectroCraftSided {

    public static ElectroCraftClient instance;
    private ComputerClient computerClient;
    private boolean runningComputerClient = false;

    public ElectroCraftClient() {
        instance = this;
    }

    public ComputerClient getComputerClient() {
        return computerClient;
    }

    public boolean usingComputerClient() {
        return runningComputerClient;
    }

    @Override
    public void init() {
    }

    @Override
    public void closeGui(Object... optionalPlayers) {
        FMLClientHandler.instance().getClient().displayGuiScreen(null);
    }

    @Override
    public void registerRenderers() {
        for (BlockRenderers renderer : BlockRenderers.values()) {
            if (renderer.getSpecialRenderer() != null)
                if (renderer.getTileClass() != null)
                    ClientRegistry.bindTileEntitySpecialRenderer(renderer.getTileClass(), renderer.getSpecialRenderer());
        }
        RenderingRegistry.registerEntityRenderingHandler(EntityDrone.class, new RenderDrone(FMLClientHandler.instance().getClient().getRenderManager(), new ModelDrone(), 0.5f));
    }

    @Override
    public Object getTickHandler() {
        return new ClientTickHandler();
    }

    @Override
    public boolean isShiftHeld() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void openComputerGui() {
        FMLClientHandler.instance().getClient().displayGuiScreen(new GuiComputerScreen());
    }

    @Override
    public void openNetworkGui(NetworkAddressPacket packet) {
        FMLClientHandler.instance().getClient().displayGuiScreen(new GuiNetworkAddressScreen(packet));
    }

    @Override
    public void startComputerClient(int port, SocketAddress address) {
        runningComputerClient = true;
        try {
            computerClient = new ComputerClient(port, address);
            new Thread(computerClient).start();
        } catch (UnknownHostException e) {
            ElectroCraft.instance.getLogger().severe("Client: Unable to connect to server: Host unknown!");
        } catch (IOException e) {
            ElectroCraft.instance.getLogger().severe("Client: Unable to connect to server!");
        }
    }

    @Override
    public void handleClientCustomPacket(CustomPacket packet) {
        GuiScreen currentScreen = FMLClientHandler.instance().getClient().currentScreen;
        if (currentScreen instanceof GuiComputerScreen) {
            GuiComputerScreen computerScreen = (GuiComputerScreen) currentScreen;
            computerScreen.handleCustomPacket(packet);
        }
    }

    @Override
    public Object getClientGuiFor(Gui gui, Object... args) {
        if (gui == Gui.DRONE_INVENTORY) {
            return new GuiDroneInventory((ContainerDrone) args[0]);
        }
        return null;
    }
}
