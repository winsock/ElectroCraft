package info.cerios.electrocraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ComputerInputPacket extends ElectroPacket {

	private int key;
	private int button;
	private boolean down = false;
	private int dX, dY, wD;
	
	public ComputerInputPacket() {
		type = Type.INPUT;
	}
	
	@Override
	protected byte[] getData() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.write(type.ordinal());
		dos.writeInt(key);
		dos.writeInt(button);
		dos.writeBoolean(down);
		dos.writeInt(dX);
		dos.writeInt(dY);
		dos.writeInt(wD);
		return bos.toByteArray();
	}

	@Override
	protected void readData(byte[] data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bis);
		dis.read(); // Throw away the type info
		key = dis.readInt();
		button = dis.readInt();
		down = dis.readBoolean();
		dX = dis.readInt();
		dY = dis.readInt();
		wD = dis.readInt();
	}
	
	public void setMouseDeltas(int deltaX, int deltaY, int wheelDelta) {
		this.dX = deltaX;
		this.dY = deltaY;
		this.wD = wheelDelta;
	}
	
	public void setWasKeyDown(boolean downState) {
		down = downState;
	}
	
	public void setEventKey(int key) {
		this.key = key;
	}
	
	public void setEventMouseButton(int button) {
		this.button = button;
	}
	
	public int getEventMouseButton() {
		return button;
	}
	
	public int getDeltaX() {
		return dX;
	}
	
	public int getDeltaY() {
		return dY;
	}
	
	public int getWheelDelta() {
		return wD;
	}
	
	public int getEventKey() {
		return key;
	}
	
	public boolean wasKeyDown() {
		return down;
	}
}
