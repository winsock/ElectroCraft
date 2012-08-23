package info.cerios.electrocraft.core.computer.luajavaapi;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.ExposedToLua;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.File;
import java.io.IOException;

import com.naef.jnlua.LuaRuntimeException;

@ExposedToLua
public class ComputerFile {

	private Computer computer;
	private File javaFile;
	
	@ExposedToLua(value = false)
	public ComputerFile(String pathname, Computer computer) {
		this.computer = computer;
		this.javaFile = new File(pathname);
		
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				throw new LuaRuntimeException("Error! Not allowed to open files outside of its base directory!");
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }

		if (pathname.contains(".persist")) {
			throw new LuaRuntimeException("Error! Not allowed to open the persist file!");
		}
		
		if (!this.exists()) {
			if (computer.getBaseDirectory().length() > ConfigHandler.getCurrentConfig().getOrCreateIntProperty("MaxStoragePerUser", "computer", 10).getInt(10) * 1024 * 1024) {
				throw new LuaRuntimeException("Error! Tried to make a new file when the disk is full!");
			}
		}
		
		if (computer.getNumberOfOpenFileHandles() >= computer.getMaxFileHandles()) {
			throw new LuaRuntimeException("Error! Tried to open too many files!");
		} else {
			computer.incrementOpenFileHandles();
		}
	}
	
	@ExposedToLua
	public boolean mkdirs() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				throw new LuaRuntimeException("Error! Not allowed to open files outside of its base directory!");
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		
		return javaFile.mkdirs();
	}
	
	@ExposedToLua
	public boolean mkdir() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		
		return javaFile.mkdir();
	}
	
	@ExposedToLua
	public boolean createNewFile() throws IOException {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return javaFile.createNewFile();
	}
	
	@ExposedToLua
	public boolean canWrite() {
		if (computer.getBaseDirectory().length() > ConfigHandler.getCurrentConfig().getOrCreateIntProperty("MaxStoragePerUser", "computer", 10).getInt(10) * 1024 * 1024) {
			return false;
		}
		return javaFile.canWrite();
	}
	
	@ExposedToLua
	public boolean canRead() {
		return javaFile.canRead();
	}
	
	@ExposedToLua
	public boolean delete() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return javaFile.delete();
	}
	
	@ExposedToLua
	public boolean exists() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return javaFile.exists();
	}
	
	@ExposedToLua
	public long getFreeSpace()  {
		return (ConfigHandler.getCurrentConfig().getOrCreateIntProperty("MaxStoragePerUser", "computer", 10).getInt(10) * 1024 * 1024) - computer.getBaseDirectory().length();
	}
	
	@ExposedToLua
	public String getPath() {
		return javaFile.getPath().replace(computer.getBaseDirectory().getAbsolutePath(), "");
	}
	
	@ExposedToLua
	public String getName() {
		return javaFile.getName();
	}
	
	@ExposedToLua
	public ComputerFile getParentFile() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile.getParentFile())) {
				return null;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return new ComputerFile(javaFile.getParent(), computer);
	}
	
	@ExposedToLua
	public boolean isFile() {
		return javaFile.isFile();
	}
	
	@ExposedToLua
	public boolean isDirectory() {
		return javaFile.isDirectory();
	}
	
	@ExposedToLua
	public long length() {
		return javaFile.length();
	}
	
	@ExposedToLua
	public ComputerFile[] listFiles() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile.getParentFile())) {
				throw new LuaRuntimeException("Error! Not allowed to open files outside of its base directory!");
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		ComputerFile[] files = new ComputerFile[javaFile.listFiles().length];
		int counter = 0;
		for (File f : javaFile.listFiles()) {
			if (f.getName().contains(".persist"))
				continue;
			files[counter++] = new ComputerFile(f.getAbsolutePath(), computer);
		}
		return files;
	}
	
	@ExposedToLua(value = false)
	public void finalize() throws Throwable {
		computer.deincrementOpenFileHandles();
		super.finalize();
	}
	
	@ExposedToLua(value = false)
	public File getJavaFile() {
		return javaFile;
	}
}
