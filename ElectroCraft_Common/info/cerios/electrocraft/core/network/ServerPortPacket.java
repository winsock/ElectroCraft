package info.cerios.electrocraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerPortPacket extends ElectroPacket {

	private int port;
	
	public ServerPortPacket() {
		this.type = Type.PORT;
	}
	
	@Override
	protected byte[] getData() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.write(type.ordinal());
		dos.writeInt(port);
		return bos.toByteArray();
	}

	@Override
	protected void readData(byte[] data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		dis.read(); // Throw away the type info
		port = dis.readInt();
	}

	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
}
