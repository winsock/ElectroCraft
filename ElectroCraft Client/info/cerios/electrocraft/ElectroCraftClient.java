package info.cerios.electrocraft;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import info.cerios.electrocraft.blocks.render.BlockRenderers;
import info.cerios.electrocraft.computer.ComputerClient;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.IElectroCraftSided;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.gui.GuiComputerScreen;
import info.cerios.electrocraft.gui.GuiNetworkAddressScreen;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;

public class ElectroCraftClient implements IElectroCraftSided {

	public static ElectroCraftClient instance;
	private ComputerClient computerClient;
	
	public ElectroCraftClient() {
		instance = this;
	}
	
	public ComputerClient getComputerClient() {
		return computerClient;
	}

	@Override
	public void init() { }
	
    @Override
    public TileEntity getBlockTileEntity(int x, int y, int z, int d) {
    	if (FMLClientHandler.instance().getClient().theWorld != null)
    		return FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(x, y, z);
    	return null;
    }

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
	public void startComputerClient(int port, SocketAddress address) {
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
	public File getBaseDir() {
		return FMLClientHandler.instance().getClient().getMinecraftDir();
	}
}
