package info.cerios.electrocraft.computer;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.jpc.emulator.pci.peripheral.DefaultVGACard;
import info.cerios.electrocraft.core.network.ComputerProtocol;
import info.cerios.electrocraft.core.utils.ObjectTriplet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.NetworkListenThread;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.server.FMLServerHandler;

public class ComputerServer implements Runnable {
    
	private volatile boolean running = true;
	private ServerSocket socket;
	private Map<EntityPlayerMP, ComputerServerClient> clients = new HashMap<EntityPlayerMP, ComputerServerClient>();
	
    public ComputerServer(int port) throws IOException {
        socket = new ServerSocket(port);
		FMLCommonHandler.instance().getFMLLogger().info("ElectroCraft ComputerServer: Running on port " + String.valueOf(port) + "!");
    }
    
    public void setRunning(boolean running) {
    	this.running = running;
    }
    
    public boolean getRunning() {
    	return running;
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
				ArrayList players = null;
				try {
					players = (ArrayList) ModLoader.getPrivateValue(NetworkListenThread.class, FMLServerHandler.instance().getServer().networkServer, "playerList");
				} catch(RuntimeException e) {
					players = (ArrayList) ModLoader.getPrivateValue(NetworkListenThread.class, FMLServerHandler.instance().getServer().networkServer, "h");
				}
				for (int i = 0; i < players.size(); i++) {
					NetServerHandler player = (NetServerHandler) players.get(i);
					if (((InetSocketAddress)player.netManager.getRemoteAddress()).getHostName().equalsIgnoreCase(connection.getInetAddress().getHostName())) {
						FMLCommonHandler.instance().getFMLLogger().info("ElectroCraft ComputerServer: Client Connected!");
						clients.put(player.getPlayerEntity(), client);
						new Thread(client).start();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
