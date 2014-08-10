package info.cerios.electrocraft.core.network;

import io.netty.buffer.ByteBuf;

public class ServerPortPacket extends ElectroPacket {

    private int port;

    public ServerPortPacket() {
        this.type = Type.PORT;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type.ordinal());
        buf.writeInt(port);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        data.readByte();
        port = data.readInt();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
