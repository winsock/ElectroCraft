package info.cerios.electrocraft.core.network;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class GuiPacket extends ElectroPacket {

    public enum Gui {
        COMPUTER_SCREEN, ADDRESS_SCREEN, DRONE_INVENTORY
    };

    private Gui windowId;
    private boolean closeWindow = false; // When set it closes the current
                                         // window

    public GuiPacket() {
        type = Type.GUI;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(new byte[] { (byte) type.ordinal(), (byte) windowId.ordinal(), (byte) (closeWindow ? 1 : 0) });
    }

    @Override
    public void fromBytes(ByteBuf data) {
        data.readByte(); // Discard type
        windowId = Gui.values()[data.readByte()];
        closeWindow = (data.readByte() == 0 ? false : true);
    }

    public void setGui(Gui gui) {
        this.windowId = gui;
    }

    public Gui getGui() {
        return windowId;
    }

    public boolean closeWindow() {
        return closeWindow;
    }

    public void setCloseWindow(boolean close) {
        this.closeWindow = close;
    }
}
