package info.cerios.electrocraft.core.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import info.cerios.electrocraft.core.ElectroCraft;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

/**
 * A packet that contains uniform data that can be serialized
 * 
 * @author andrewquerol
 */
public abstract class ElectroPacket implements IMessage {

    public enum Type {
        MODIFIER(ModifierPacket.class), GUI(GuiPacket.class), ADDRESS(NetworkAddressPacket.class), INPUT(ComputerInputPacket.class), PORT(ServerPortPacket.class), CUSTOM(CustomPacket.class);

        private Class<? extends ElectroPacket> packetClass;

        private Type(Class<? extends ElectroPacket> packetClass) {
            this.packetClass = packetClass;
        }
    }

    protected Type type;

    public abstract void fromBytes(ByteBuf buf);

    public abstract void toBytes(ByteBuf buf);

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
