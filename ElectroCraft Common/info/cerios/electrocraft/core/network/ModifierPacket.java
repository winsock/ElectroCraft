package info.cerios.electrocraft.core.network;

import java.io.IOException;

public class ModifierPacket extends ElectroPacket {

    private boolean isShiftDown = false, isCtrlDown = false;

    public ModifierPacket() {
        type = Type.MODIFIER;
    }

    @Override
    protected byte[] getData() throws IOException {
        return new byte[]{(byte) type.ordinal(), (byte) (isShiftDown ? 1 : 0), (byte) (isCtrlDown ? 1 : 0)};
    }

    @Override
    protected void readData(byte[] data) throws IOException {
    	isShiftDown = (data[1] == 0 ? false : true);
    	isCtrlDown = (data[2] == 0 ? false : true);
    }

    public void setModifiers(boolean shift, boolean ctrl) {
    	this.isCtrlDown = ctrl;
    	this.isShiftDown = shift;
    }
    
    public boolean isShiftDown() {
    	return this.isShiftDown;
    }
    
    public boolean isCtrlDown() {
    	return this.isCtrlDown;
    }
}
