package info.cerios.electrocraft.core.network;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;

import java.io.IOException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetLoginHandler;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet1Login;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class ConnectionHandler implements IConnectionHandler {

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, NetworkManager manager) {
		if (!ConfigHandler.getCurrentConfig().get("general", "useMCServer", false).getBoolean(false)) {
			ServerPortPacket portPacket = new ServerPortPacket();
			portPacket.setPort(ElectroCraft.instance.getServer().getPort());
			try {
				manager.addToSendQueue(portPacket.getMCPacket());
			} catch (IOException e) {
				ElectroCraft.instance.getLogger().severe("Unable to send computer servers port!");
			}    	
		}
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, NetworkManager manager) {
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, NetworkManager manager) {
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, NetworkManager manager) {
	}

	@Override
	public void connectionClosed(NetworkManager manager) {
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, NetworkManager manager, Packet1Login login) {
	}
}
