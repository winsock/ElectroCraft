package info.cerios.electrocraft.core.network;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;

import java.io.IOException;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class ConnectionHandler implements IConnectionHandler {

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
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
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
        return null;
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
    }

    @Override
    public void connectionClosed(INetworkManager manager) {
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
    }
}
