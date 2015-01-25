package info.cerios.electrocraft.core.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import info.cerios.electrocraft.core.ElectroCraft;
import io.netty.buffer.ByteBuf;

public class ServerPortPacket extends ElectroPacket implements IMessageHandler<ServerPortPacket, IMessage> {

    private int port;

    public ServerPortPacket() {
        this.type = Type.PORT;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(port);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        port = data.readInt();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public IMessage onMessage(ServerPortPacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            ElectroCraft.electroCraftSided.startComputerClient(message.getPort(), ctx.getClientHandler().getNetworkManager().getRemoteAddress());
        }
        return null;
    }
}
