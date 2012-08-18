package info.cerios.electrocraft.core.network;

import info.cerios.electrocraft.ElectroCraftSidedServer;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class UniversialPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {
		if (!packet.channel.equalsIgnoreCase("electrocraft"))
			return;
		if (player instanceof EntityPlayerMP) {
			// Must be a server sided packet
			try {
				ElectroPacket ecPacket = ElectroPacket.readMCPacket(packet);
				if (ecPacket.getType() == Type.MODIFIER) {
					ModifierPacket modifierPacket = (ModifierPacket)ecPacket;
					
					// Set the server shift state
					if (ElectroCraft.electroCraftSided instanceof ElectroCraftSidedServer)
						((ElectroCraftSidedServer)ElectroCraft.electroCraftSided).setShiftState(modifierPacket.isShiftDown());
					
					// Send the modifier packet to the computer if it is a valid computer
					if (ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player) != null) {
						if (ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player).getComputer() != null) {
							if (ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player).getComputer().getComputer() != null) {
								if (ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player).getComputer().getComputer().isRunning()) {
									ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player).getComputer().getComputer().getKeyboard().proccessModifierPacket(modifierPacket);
								}
							}
						}
					}
				} else if (ecPacket.getType() == Type.ADDRESS) {
					NetworkAddressPacket addressPacket = (NetworkAddressPacket)ecPacket;
					World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(addressPacket.getWorldId());
					TileEntity tileEntity = world.getBlockTileEntity(addressPacket.getX(), addressPacket.getY(), addressPacket.getZ());
					if(tileEntity instanceof NetworkBlock) {
						((NetworkBlock)tileEntity).setControlAddress(addressPacket.getControlAddress());
						((NetworkBlock)tileEntity).setDataAddress(addressPacket.getDataAddress());
					}
				} else if (ecPacket.getType() == Type.INPUT) {
					ComputerInputPacket inputPacket = (ComputerInputPacket)ecPacket;
					if (inputPacket.wasKeyDown()) {
						ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player).getComputer().getComputer().getKeyboard().onKeyPress(inputPacket);
					}
				}
			} catch (IOException e) {
				FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to parse packet send on our channel!");
			}
		} else {
			// Must be a client sided packet
			try {
	        	ElectroPacket ecPacket = ElectroPacket.readMCPacket(packet);
	            if (ecPacket.getType() == Type.GUI) {
	                GuiPacket guiPacket = (GuiPacket) ecPacket;
	                if (guiPacket.closeWindow())
	                    ElectroCraft.instance.electroCraftSided.closeGui();
	                else if (guiPacket.getGui() == Gui.COMPUTER_SCREEN) {
	                    ElectroCraft.electroCraftSided.openComputerGui();
	                }
	            } else if (ecPacket.getType() == Type.ADDRESS) {
	                NetworkAddressPacket networkPacket = (NetworkAddressPacket) ecPacket;
                    ElectroCraft.electroCraftSided.openNetworkGui(networkPacket);
	            } else if (ecPacket.getType() == Type.PORT) {
	                ServerPortPacket portPacket = (ServerPortPacket) ecPacket;
                    ElectroCraft.electroCraftSided.startComputerClient(portPacket.getPort(), manager.getSocketAddress());
	            }
	        } catch (IOException e) {
	            FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to read packet sent on our channel!");
	        }
		}
	}
}
