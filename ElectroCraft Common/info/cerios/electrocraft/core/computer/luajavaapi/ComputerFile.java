package info.cerios.electrocraft.core.computer.luajavaapi;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.ExposedToLua;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.File;
import java.io.IOException;

import com.naef.jnlua.LuaRuntimeException;

@ExposedToLua
public class ComputerFile extends File {

	private Computer computer;
	
	@ExposedToLua(value = false)
	public ComputerFile(String pathname, Computer computer) {
		super(pathname);
		this.computer = computer;
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), this)) {
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
	@Override
	public boolean mkdirs() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), this)) {
				throw new LuaRuntimeException("Error! Not allowed to open files outside of its base directory!");
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		
		return super.mkdirs();
	}
	
	@ExposedToLua
	@Override
	public boolean mkdir() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), this)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		
		return super.mkdir();
	}
	
	@ExposedToLua
	@Override
	public boolean canWrite() {
		if (computer.getBaseDirectory().length() > ConfigHandler.getCurrentConfig().getOrCreateIntProperty("MaxStoragePerUser", "computer", 10).getInt(10) * 1024 * 1024) {
			return false;
		}
		return super.canWrite();
	}
	
	@ExposedToLua
	@Override
	public boolean canRead() {
		return super.canRead();
	}
	
	@ExposedToLua
	@Override
	public boolean delete() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), this)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return super.delete();
	}
	
	@ExposedToLua
	@Override
	public boolean exists() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), this)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return super.exists();
	}
	
	@ExposedToLua
	@Override
	public long getFreeSpace()  {
		return (ConfigHandler.getCurrentConfig().getOrCreateIntProperty("MaxStoragePerUser", "computer", 10).getInt(10) * 1024 * 1024) - computer.getBaseDirectory().length();
	}
	
	@ExposedToLua
	@Override
	public String getPath() {
		return super.getAbsolutePath().replace(computer.getBaseDirectory().getAbsolutePath(), "");
	}
	
	@ExposedToLua
	@Override
	public String getName() {
		return super.getName();
	}
	
	@ExposedToLua
	@Override
	public ComputerFile getParentFile() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), super.getParentFile())) {
				return null;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return new ComputerFile(super.getParent(), computer);
	}
	
	@ExposedToLua
	@Override
	public boolean isFile() {
		return super.isFile();
	}
	
	@ExposedToLua
	@Override
	public boolean isDirectory() {
		return super.isDirectory();
	}
	
	@ExposedToLua
	@Override
	public long length() {
		return super.length();
	}
	
	@ExposedToLua
	@Override
	public ComputerFile[] listFiles() {
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), super.getParentFile())) {
				throw new LuaRuntimeException("Error! Not allowed to open files outside of its base directory!");
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		ComputerFile[] files = new ComputerFile[super.listFiles().length];
		int counter = 0;
		for (File f : super.listFiles()) {
			if (f.getName().contains(".persist"))
				continue;
			files[counter++] = new ComputerFile(f.getAbsolutePath(), computer);
		}
		return files;
	}
	
	@ExposedToLua(value = false)
	@Override
	public void finalize() throws Throwable {
		computer.deincrementOpenFileHandles();
		super.finalize();
	}
}
