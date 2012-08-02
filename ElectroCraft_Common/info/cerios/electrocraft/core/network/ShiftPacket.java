package info.cerios.electrocraft.core.network;

import java.io.IOException;

public class ShiftPacket extends ElectroPacket {

	private boolean shiftState = false;
	
	public ShiftPacket() {
		type = Type.SHIFT;
	}
	
	@Override
	protected byte[] getData() throws IOException {
		return new byte[] { (byte) type.ordinal(), (byte) (shiftState ? 1 : 0) };
	}

	@Override
	protected void readData(byte[] data) throws IOException {
		shiftState = (data[1] == 0 ? false : true);
	}

	public boolean getShiftState() {
		return shiftState;
	}
	
	public void setShiftState(boolean shiftState) {
		this.shiftState = shiftState;
	}
}
