package info.cerios.electrocraft.core.network;

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
	protected byte[] getData() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.write(type.ordinal());
		dos.writeInt(id);
		dos.writeInt(data.length);
		dos.write(data);
		return bos.toByteArray();
	}

	@Override
	protected void readData(byte[] data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		dis.read(); // Throw away the type info
		id = dis.readInt();
		int length = dis.readInt();
		this.data = new byte[length];
		dis.read(this.data, 0, length);
	}
}
