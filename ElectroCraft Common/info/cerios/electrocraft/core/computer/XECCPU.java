package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.computer.XECInterface.AssembledData;

public class XECCPU {
	
	private final long internalID;
	
	private XECCPU(long id) {
		this.internalID = id;
	}
	
	public native XECVGACard getVideoCard();

    public native XECTerminal getTerminal();

    public native XECKeyboard getKeyboard();

    public native AssembledData assemble(String data);

    public native long loadIntoMemory(byte[] data, int length, int codeStart);

    public native void manualTick();

    public native void start(long baseAddress);

    public native void stop();

    public native void reset(long baseAddress);
    
    public native boolean isRunning();
    
    public native void registerInterupt(int port, IComputerCallback callback);
    
    public native void removeInterupt(int port);
        
    public static class InteruptData {
    	public int interuptPort = 0;
    	public int data = 0;
    	public boolean read = false;
    }
}