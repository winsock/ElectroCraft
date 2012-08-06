package info.cerios.electrocraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;

/**
 * A packet that contains uniform data that can be serialized
 * @author andrewquerol
 *
 * @param <T extends Serializable> The type of data
 */
public abstract class ElectroPacket {
	
	public enum Type {
		SHIFT(ShiftPacket.class),
		GUI(GuiPacket.class),
		ADDRESS(NetworkAddressPacket.class),
		INPUT(ComputerInputPacket.class),
		PORT(ServerPortPacket.class);
		
		private Class<? extends ElectroPacket> packetClass; 
		
		private Type(Class<? extends ElectroPacket> packetClass) {
			this.packetClass = packetClass;
		}
	};
	
	protected Type type;
		
	protected abstract byte[] getData() throws IOException;
	
	protected abstract void readData(byte[] data) throws IOException;
	
	public Packet250CustomPayload getMCPacket() throws IOException {
		Packet250CustomPayload mcPacket = new Packet250CustomPayload();
		mcPacket.channel = "electrocraft";
		byte[] data = getData();
		mcPacket.length = data.length;
		mcPacket.data = data;
		return mcPacket;
	}
	
	public static ElectroPacket readMCPacket(Packet250CustomPayload packet) throws IOException {
		ElectroPacket ecPacket = getAndCreatePacketFromId(packet.data[0]);
		ecPacket.readData(packet.data);
		return ecPacket;
	}
	
	public Type getType() {
		return type;
	}
	
	private static ElectroPacket getAndCreatePacketFromId(byte id) {
		try {
			return Type.values()[id].packetClass.newInstance();
		} catch (Exception e) { FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to parse packet id!"); }
		return null;
	}
}
