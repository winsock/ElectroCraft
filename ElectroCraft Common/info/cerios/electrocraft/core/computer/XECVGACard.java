package info.cerios.electrocraft.core.computer;

public class XECVGACard {
    public native void clearScreen();

    public native byte[] getScreenData();

    public native void setScreenSize(int width, int height);

    public native void manualTick();

    public native int getScreenWidth();

    public native int getScreenHeight();

    private final long internalID; // Used to interface back with C++

    private XECVGACard(long internalID) {
        this.internalID = internalID;
    }
}