package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.utils.Utils;

import java.io.File;
import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;

public class XECInterface {
	
	public XECInterface() {
		String osName = System.getProperty("os.name");
		String fileExtention = "so";
		if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1) {
			fileExtention = "dll";
		} else if (System.getProperty("os.name").toUpperCase().indexOf("LINUX") > -1) {
			fileExtention = "so";
		} else if (System.getProperty("os.name").toUpperCase().indexOf("MAC") > -1) {
			fileExtention = "dylib";
		}

		File libFolder = new File(FMLCommonHandler.instance().getMinecraftRootDirectory().getAbsolutePath() + File.separator + "electrocraft" + File.separator + "natives");
		if (!libFolder.exists())
			libFolder.mkdirs();
		
		File libraryFile = new File(libFolder.getAbsolutePath() + File.separator + "libElectroCraftCPU." + fileExtention);
		if (!libraryFile.exists()) {
			try {
				Utils.copyResource("info/cerios/electrocraft/core/natives/libElectroCraftCPU." + fileExtention, libraryFile);
			} catch (IOException e) {
				FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Error copying computer library! Computers will not work!");
			}
		}
		
		try {
			System.load(libFolder.getAbsolutePath() + File.separator + "libElectroCraftCPU." + fileExtention);
		} catch (Exception e) {
			FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Error loading computer library! Computers will not work!");
		}
	}
	
	public static class AssembledData {
		public byte[] data;
		public int length;
		public int codeStart;
	}
	
	public native void createCPU();
	public native XECVGACard getVideoCard();
	public native XECTerminal getTerminal();
	public native XECKeyboard getKeyboard();
	public native AssembledData assemble(String data);
    public native long loadIntoMemory(byte[] data, int length, int codeStart);
    public native void manualTick();
    public native void start(long baseAddress);
    public native void stop();
    public native void reset(long baseAddress);
}
