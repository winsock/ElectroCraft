package info.cerios.electrocraft.core.computer;

public class XECKeyboard {
	
	public native void onKeyPress(byte key);
	
	private final long internalID;
	
	private XECKeyboard(long id) {
		internalID = id;
	}
}