package info.cerios.electrocraft.core.network;

import info.cerios.electrocraft.core.ElectroCraft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.src.EntityPlayerMP;
import cpw.mods.fml.common.FMLCommonHandler;

public class ComputerServer implements Runnable {
    
	private volatile boolean running = true;
	private ServerSocket socket;
	private Map<EntityPlayerMP, ComputerServerClient> clients = new HashMap<EntityPlayerMP, ComputerServerClient>();
	
    public ComputerServer(int port) throws IOException {
        socket = new ServerSocket(port);
		ElectroCraft.instance.getLogger().info("ComputerServer: Running on port " + String.valueOf(port) + "!");
    }
    
    public void setRunning(boolean running) {
    	this.running = running;
    }
    
    public boolean getRunning() {
    	return running;
    }
    
    public int getPort() {
    	return socket.getLocalPort();
    }
    
    public ComputerServerClient getClient(EntityPlayerMP player) {
    	return clients.get(player);
    }

	@Override
	public void run() {
		while (running) {
			try {
				Socket connection = socket.accept();
				ComputerServerClient client = new ComputerServerClient(this, connection);
				List players = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList;

				for (int i = 0; i < players.size(); i++) {
					EntityPlayerMP player = (EntityPlayerMP) players.get(i);
					if (((InetSocketAddress)player.playerNetServerHandler.netManager.getSocketAddress()).getHostName().equalsIgnoreCase(connection.getInetAddress().getHostName())) {
						ElectroCraft.instance.getLogger().info("ComputerServer: Client Connected!");
						clients.put(player, client);
						new Thread(client).start();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
