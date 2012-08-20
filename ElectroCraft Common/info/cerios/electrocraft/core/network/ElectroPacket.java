package info.cerios.electrocraft.core.network;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.src.Packet250CustomPayload;

import info.cerios.electrocraft.core.ElectroCraft;

import java.io.IOException;

/**
 * A packet that contains uniform data that can be serialized
 *
 * @author andrewquerol
 */
public abstract class ElectroPacket {

    public enum Type {
        MODIFIER(ModifierPacket.class),
        GUI(GuiPacket.class),
        ADDRESS(NetworkAddressPacket.class),
        INPUT(ComputerInputPacket.class),
        PORT(ServerPortPacket.class);

        private Class<? extends ElectroPacket> packetClass;

        private Type(Class<? extends ElectroPacket> packetClass) {
            this.packetClass = packetClass;
        }
    }
    
    protected Type type;

    protected abstract byte[] getData() throws IOException;

    protected abstract void readData(byte[] data) throws IOException;

    public Packet250CustomPayload getMCPacket() throws IOException {
        Packet250CustomPayload mcPacket = new Packet250CustomPayload();
        mcPacket.channel = "electrocraft";
        byte[] data = getData();
        mcPacket.length = data.length;
        mcPacket.data = data;
        mcPacket.isChunkDataPacket = false;
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
        } catch (Exception e) {
            ElectroCraft.instance.getLogger().severe("Unable to parse packet id!");
        }
        return null;
    }
}
