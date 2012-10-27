package info.cerios.electrocraft;

import java.io.File;
import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.IScheduledTickHandler;
import info.cerios.electrocraft.blocks.render.BlockRenderers;
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
import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiScreen;
import net.minecraftforge.client.MinecraftForgeClient;

public class ElectroCraftClient implements IElectroCraftSided {

	public static ElectroCraftClient instance;
	
	public ElectroCraftClient() {
		instance = this;
	}

	@Override
	public void init() { }

    @Override
    public void closeGui(Object... optionalPlayers) {
        FMLClientHandler.instance().getClient().displayGuiScreen(null);
    }

	@Override
	public void registerRenderers() {
		for (BlockRenderers renderer : BlockRenderers.values()) {
			if (renderer.getRenderer() != null)
				RenderingRegistry.registerBlockHandler(renderer.getRenderer());
			if (renderer.getSpecialRenderer() != null)
				if (renderer.getTileClass() != null)
					ClientRegistry.bindTileEntitySpecialRenderer(renderer.getTileClass(), renderer.getSpecialRenderer());
		}
		RenderingRegistry.registerEntityRenderingHandler(EntityDrone.class, new RenderDrone(new ModelDrone(), 0.5f));
	}

	@Override
	public int getFreeRenderId() {
		return RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public IScheduledTickHandler getTickHandler() {
		return new ClientTickHandler();
	}

	@Override
	public void loadTextures() {
        MinecraftForgeClient.preloadTexture("/info/cerios/electrocraft/gfx/blocks.png");
        MinecraftForgeClient.preloadTexture("/info/cerios/electrocraft/gfx/items.png");
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
	public File getBaseDir() {
		FMLClientHandler.instance().getClient();
		return Minecraft.getMinecraftDir();
	}

	@Override
	public void handleClientCustomPacket(CustomPacket packet) {
		GuiScreen currentScreen = FMLClientHandler.instance().getClient().currentScreen;
		if (currentScreen instanceof GuiComputerScreen) {
			GuiComputerScreen computerScreen = (GuiComputerScreen)currentScreen;
			computerScreen.handleCustomPacket(packet);
		}
	}

	@Override
	public Object getClientGuiFor(Gui gui, Object... args) {
		if (gui == Gui.DRONE_INVENTORY) {
			return new GuiDroneInventory((ContainerDrone)args[0]);
		}
		return null;
	}
}
