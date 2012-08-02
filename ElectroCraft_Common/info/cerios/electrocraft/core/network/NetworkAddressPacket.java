package info.cerios.electrocraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetworkAddressPacket extends ElectroPacket {

	private int dataAddress;
	private int controlAddress;
	private int world, x, y, z;
	
	public NetworkAddressPacket() {
		type = Type.ADDRESS;
	}
	
	@Override
	protected byte[] getData() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.write(type.ordinal());
		dos.writeInt(x);
		dos.writeInt(y);
		dos.writeInt(z);
		dos.writeInt(world);
		dos.writeInt(dataAddress);
		dos.writeInt(controlAddress);
		return bos.toByteArray();
	}

	@Override
	protected void readData(byte[] data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		dis.read(); // Throw away the type info
		x = dis.readInt();
		y = dis.readInt();
		z = dis.readInt();
		world = dis.readInt();
		dataAddress = dis.readInt();
		controlAddress = dis.readInt();
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
