package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.jpc.emulator.HardwareComponent;
import info.cerios.electrocraft.core.jpc.support.BlockDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IComputer {
	public void addPart(HardwareComponent part);
    public void removePart(HardwareComponent part);
    public void start();
    public void stop();
    public void changeFloppyDisk(BlockDevice disk, int index);
    public void saveState(OutputStream out) throws IOException;
    public void loadState(InputStream in) throws IOException;
    public void reset();
    public HardwareComponent getComponent(Class<? extends HardwareComponent> cls);
    public int execute();
    public int executeReal();
    public int executeProtected();
    public int executeVirtual8086();
    
}
