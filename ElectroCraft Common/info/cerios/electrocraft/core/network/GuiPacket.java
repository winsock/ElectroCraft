package info.cerios.electrocraft.core.network;

import java.io.IOException;

public class GuiPacket extends ElectroPacket {

    public enum Gui { COMPUTER_SCREEN, ADDRESS_SCREEN, DRONE_INVENTORY };

    private Gui windowId;
    private boolean closeWindow = false; // When set it closes the current window

    public GuiPacket() {
        type = Type.GUI;
    }

    @Override
    protected byte[] getData() throws IOException {
        return new byte[]{(byte) type.ordinal(), (byte) windowId.ordinal(), (byte) (closeWindow ? 1 : 0)};
    }

    @Override
    protected void readData(byte[] data) throws IOException {
        windowId = Gui.values()[data[1]];
        closeWindow = (data[2] == 0 ? false : true);
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
