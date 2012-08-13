package info.cerios.electrocraft;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.network.ComputerInputPacket;
import info.cerios.electrocraft.core.network.ElectroPacket;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.core.network.ServerPortPacket;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.ShiftPacket;

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
import cpw.mods.fml.server.FMLServerHandler;

public class ServerPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player) {
		if (!packet.channel.equalsIgnoreCase("electrocraft"))
			return;
		try {
			ElectroPacket ecPacket = ElectroPacket.readMCPacket(packet);
			if (ecPacket.getType() == Type.SHIFT) {
				ShiftPacket shiftPacket = (ShiftPacket)ecPacket;
				
			} else if (ecPacket.getType() == Type.ADDRESS) {
				NetworkAddressPacket addressPacket = (NetworkAddressPacket)ecPacket;
				World world = FMLServerHandler.instance().getServer().worldServerForDimension(addressPacket.getWorldId());
				TileEntity tileEntity = world.getBlockTileEntity(addressPacket.getX(), addressPacket.getY(), addressPacket.getZ());
				if(tileEntity instanceof NetworkBlock) {
					((NetworkBlock)tileEntity).setControlAddress(addressPacket.getControlAddress());
					((NetworkBlock)tileEntity).setDataAddress(addressPacket.getDataAddress());
				}
			} else if (ecPacket.getType() == Type.INPUT) {
				ComputerInputPacket inputPacket = (ComputerInputPacket)ecPacket;
				
			}
		} catch (IOException e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to parse packet send on our channel!");
		}
	}
}
