package info.cerios.electrocraft.core.computer;

public class XECTerminal {
	public native String getLine(int line);
	public native int getColoumns();
	public native int getRows();
	public native void clear();
	
	private final long internalID;
	
	private XECTerminal(long id) {
		internalID = id;
	}
}
