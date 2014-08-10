package info.cerios.electrocraft.core.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

public class ComputerInputPacket extends ElectroPacket {

    private int key;
    private String keyName;
    private int button;
    private boolean down = false;
    private int dX, dY, wD;
    private boolean upKey = false;
    private boolean downKey = false;
    private boolean leftKey = false;
    private boolean rightKey = false;

    public ComputerInputPacket() {
        type = Type.INPUT;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type.ordinal());
        buf.writeInt(key);
        ByteBufUtils.writeUTF8String(buf, keyName);
        buf.writeInt(button);
        buf.writeBoolean(down);
        buf.writeBoolean(leftKey);
        buf.writeBoolean(rightKey);
        buf.writeBoolean(upKey);
        buf.writeBoolean(downKey);
        buf.writeInt(dX);
        buf.writeInt(dY);
        buf.writeInt(wD);
    }

    @Override
    public void fromBytes(ByteBuf data) {
        data.readByte(); // Throw away the type info
        key = data.readInt();
        keyName = ByteBufUtils.readUTF8String(data);
        button = data.readInt();
        down = data.readBoolean();
        leftKey = data.readBoolean();
        rightKey = data.readBoolean();
        upKey = data.readBoolean();
        downKey = data.readBoolean();
        dX = data.readInt();
        dY = data.readInt();
        wD = data.readInt();
    }

    public void setMouseDeltas(int deltaX, int deltaY, int wheelDelta) {
        this.dX = deltaX;
        this.dY = deltaY;
        this.wD = wheelDelta;
    }

    public void setWasKeyDown(boolean downState) {
        down = downState;
    }

    public void setLeftArrowKey() {
        leftKey = true;
    }

    public void setRightArrowKey() {
        rightKey = true;
    }

    public void setUpArrowKey() {
        upKey = true;
    }

    public void setDownArrowKey() {
        downKey = true;
    }

    public boolean getLeftArrowKey() {
        return leftKey;
    }

    public boolean getRightArrowKey() {
        return rightKey;
    }

    public boolean getUpArrowKey() {
        return upKey;
    }

    public boolean getDownArrowKey() {
        return downKey;
    }

    public void setEventKey(int key) {
        this.key = key;
    }

    public void setEventKeyName(String name) {
        this.keyName = name;
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

    public String getEventKeyName() {
        return keyName;
    }

    public boolean wasKeyDown() {
        return down;
    }
}
