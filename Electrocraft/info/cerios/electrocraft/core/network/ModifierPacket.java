package info.cerios.electrocraft.core.network;

import io.netty.buffer.ByteBuf;

public class ModifierPacket extends ElectroPacket {

    private boolean isShiftDown = false, isCtrlDown = false;

    public ModifierPacket() {
        type = Type.MODIFIER;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(new byte[] { (byte) type.ordinal(), (byte) (isShiftDown ? 1 : 0), (byte) (isCtrlDown ? 1 : 0) });
    }

    @Override
    public void fromBytes(ByteBuf data) {
        data.readByte(); // Discard type
        isShiftDown = (data.readByte() == 0 ? false : true);
        isCtrlDown = (data.readByte() == 0 ? false : true);
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
