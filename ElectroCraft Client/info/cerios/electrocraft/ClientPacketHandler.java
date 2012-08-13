package info.cerios.electrocraft;

import info.cerios.electrocraft.computer.ComputerClient;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.ElectroPacket;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.core.network.ServerPortPacket;
import info.cerios.electrocraft.gui.GuiComputerScreen;
import info.cerios.electrocraft.gui.GuiNetworkAddressScreen;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ClientPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {
	        try {
	        	ElectroPacket ecPacket = ElectroPacket.readMCPacket(packet);
	            if (ecPacket.getType() == Type.GUI) {
	                GuiPacket guiPacket = (GuiPacket) ecPacket;
	                if (guiPacket.closeWindow())
	                    ElectroCraft.instance.electroCraftSided.closeGui();
	                else if (guiPacket.getGui() == Gui.COMPUTER_SCREEN) {
	                    FMLClientHandler.instance().getClient().displayGuiScreen(new GuiComputerScreen());
	                }
	            } else if (ecPacket.getType() == Type.ADDRESS) {
	                NetworkAddressPacket networkPacket = (NetworkAddressPacket) ecPacket;
	                FMLClientHandler.instance().getClient().displayGuiScreen(new GuiNetworkAddressScreen(networkPacket));
	            } else if (ecPacket.getType() == Type.PORT) {
	                ServerPortPacket portPacket = (ServerPortPacket) ecPacket;
	                try {
                        ComputerClient client = new ComputerClient(portPacket.getPort(), manager.getSocketAddress());
	                	new Thread(client).start();
	                    ElectroCraftClient.instance.setComputerClient(client);
	                } catch (UnknownHostException e) {
	                    FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft ComputerClient: Unable to find remote host!");
	                } catch (IOException e) {
	                    FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft ComputerClient: Unable to connect to remote host!");
	                }
	            }
	        } catch (IOException e) {
	            FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to read packet sent on our channel!");
	        }
	}

}
