package info.cerios.electrocraft;

import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import info.cerios.electrocraft.computer.ComputerServer;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.IElectroCraftSided;
import info.cerios.electrocraft.core.ServerTickHandler;
import info.cerios.electrocraft.core.network.GuiPacket;

public class ElectroCraftServer implements IElectroCraftSided {

	public static ElectroCraftServer instance;
	public ComputerServer server;
	
	private boolean isShiftHeld = false;
	private ServerPacketHandler packetHandler;
	
	public ElectroCraftServer() {
		instance = this;
	}
	
	public ComputerServer getComputerServer() {
		return server;
	}
	
	@Override
	public void init() {
		packetHandler = new ServerPacketHandler();
		try {
			server = new ComputerServer(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("serverport", "general", 1337).getInt(1337));
			new Thread(server).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public TileEntity getBlockTileEntity(int x, int y, int z, int d) {
		return FMLServerHandler.instance().getServer().worldServerForDimension(d).getBlockTileEntity(x, y, z);
	}

	@Override
	public void closeGui(Object... optionalPlayers) {
		for (Object o : optionalPlayers) {
			if (o instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) o;
				// Close out of the computer monitor screen
				GuiPacket packet = new GuiPacket();
				packet.setCloseWindow(true);
				try {
					player.serverForThisPlayer.sendPacketToPlayer(packet.getMCPacket());
				} catch (IOException e) {
					FMLCommonHandler.instance().getFMLLogger().fine("ElectroCraft: Unable to send a close Gui packet!");
				}
			}
		}
	}

	@Override
	public IScheduledTickHandler getTickHandler() {
		return new ServerTickHandler();
	}
	
	@Override
	public boolean isShiftHeld() {
		return isShiftHeld;
	}

	@Override
	public IPacketHandler getPacketHandler() {
		return packetHandler;
	}
	
	//											 //
	// Methods that are only used on client side //
	//											 //
	@Override
	public void registerRenderers() {}

	@Override
	public int getFreeRenderId() { return 0; }

	@Override
	public void loadTextures() {}
}
