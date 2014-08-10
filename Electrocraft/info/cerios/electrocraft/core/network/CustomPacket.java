package info.cerios.electrocraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CustomPacket extends ElectroPacket {
    public int id = 0;
    public byte[] data;

    public CustomPacket() {
        type = Type.CUSTOM;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        buf.writeByte(type.ordinal());
        buf.writeInt(id);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        data.readByte(); // Throw away the type info
        id = data.readInt();
        int length = data.readInt();
        this.data = new byte[length];
        data.readBytes(data, length);
    }
}
