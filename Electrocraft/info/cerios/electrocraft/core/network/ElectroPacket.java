package info.cerios.electrocraft.core.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
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
        MODIFIER, GUI, ADDRESS, INPUT, PORT, CUSTOM;
    }

    public static void registerClasses() {
        ElectroCraft.instance.getNetworkWrapper().registerMessage(ServerPortPacket.class, ServerPortPacket.class, Type.PORT.ordinal(), Side.CLIENT);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(NetworkAddressPacket.class, NetworkAddressPacket.class, Type.ADDRESS.ordinal(), Side.CLIENT);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(NetworkAddressPacket.class, NetworkAddressPacket.class, Type.ADDRESS.ordinal(), Side.SERVER);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(ModifierPacket.class, ModifierPacket.class, Type.MODIFIER.ordinal(), Side.SERVER);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(GuiPacket.class, GuiPacket.class, Type.GUI.ordinal(), Side.CLIENT);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(GuiPacket.class, GuiPacket.class, Type.GUI.ordinal(), Side.SERVER);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(CustomPacket.class, CustomPacket.class, Type.CUSTOM.ordinal(), Side.CLIENT);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(CustomPacket.class, CustomPacket.class, Type.CUSTOM.ordinal(), Side.SERVER);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(ComputerInputPacket.class, ComputerInputPacket.class, Type.INPUT.ordinal(), Side.CLIENT);
        ElectroCraft.instance.getNetworkWrapper().registerMessage(ComputerInputPacket.class, ComputerInputPacket.class, Type.INPUT.ordinal(), Side.SERVER);
    }

    protected Type type;

    public abstract void fromBytes(ByteBuf buf);

    public abstract void toBytes(ByteBuf buf);

    public Type getType() {
        return type;
    }
}
