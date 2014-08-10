package info.cerios.electrocraft.core.network;

import io.netty.buffer.ByteBuf;

public class NetworkAddressPacket extends ElectroPacket {

    private int dataAddress;
    private int controlAddress;
    private int world, x, y, z;

    public NetworkAddressPacket() {
        type = Type.ADDRESS;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type.ordinal());
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(world);
        buf.writeInt(dataAddress);
        buf.writeInt(controlAddress);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        data.readByte(); // Throw away the type info
        x = data.readInt();
        y = data.readInt();
        z = data.readInt();
        world = data.readInt();
        dataAddress = data.readInt();
        controlAddress = data.readInt();
    }

    public int getDataAddress() {
        return dataAddress;
    }

    public void setDataAddress(int dataAddress) {
        this.dataAddress = dataAddress;
    }

    public int getControlAddress() {
        return controlAddress;
    }

    public void setControlAddress(int controlAddress) {
        this.controlAddress = controlAddress;
    }

    public void setLocation(int world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getWorldId() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
