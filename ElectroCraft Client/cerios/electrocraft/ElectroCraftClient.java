package info.cerios.electrocraft;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import info.cerios.electrocraft.blocks.render.BlockRenderers;
import info.cerios.electrocraft.computer.ComputerClient;
import info.cerios.electrocraft.core.IElectroCraftSided;
import net.minecraft.src.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;

public class ElectroCraftClient implements IElectroCraftSided {

	public static ElectroCraftClient instance;
	private ComputerClient computerClient;
	private ClientPacketHandler packetHandler;
	
	public ElectroCraftClient() {
		instance = this;
	}
	
	public void setComputerClient(ComputerClient computerClient) {
		this.computerClient = computerClient;
	}
	
	public ComputerClient getComputerClient() {
		return computerClient;
	}

	@Override
	public void init() {
		packetHandler = new ClientPacketHandler();
	}

	@Override
	public IPacketHandler getPacketHandler() {
		return packetHandler;
	}
	
    @Override
    public TileEntity getBlockTileEntity(int x, int y, int z, int d) {
        return FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(x, y, z);
    }

    @Override
    public void closeGui(Object... optionalPlayers) {
        FMLClientHandler.instance().getClient().displayGuiScreen(null);
    }

	@Override
	public void registerRenderers() {
		for (BlockRenderers renderer : BlockRenderers.values()) {
			RenderingRegistry.instance().registerBlockHandler(renderer.getRenderer());
		}
	}

	@Override
	public int getFreeRenderId() {
		return RenderingRegistry.instance().getNextAvailableRenderId();
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
}
