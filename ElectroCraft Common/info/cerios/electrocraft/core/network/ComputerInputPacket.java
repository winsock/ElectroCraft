package info.cerios.electrocraft.core.network;

import java.io.*;

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
    protected byte[] getData() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.write(type.ordinal());
        dos.writeInt(key);
        dos.writeUTF(keyName);
        dos.writeInt(button);
        dos.writeBoolean(down);
        dos.writeBoolean(leftKey);
        dos.writeBoolean(rightKey);
        dos.writeBoolean(upKey);
        dos.writeBoolean(downKey);
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
        keyName = dis.readUTF();
        button = dis.readInt();
        down = dis.readBoolean();
        leftKey = dis.readBoolean();
        rightKey = dis.readBoolean();
        upKey = dis.readBoolean();
        downKey = dis.readBoolean();
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
