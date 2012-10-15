package info.cerios.electrocraft.core.computer.luaapi;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.ExposedToLua;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

@ExposedToLua
public class ComputerFile implements LuaAPI {

	private Computer computer;
	private File javaFile;
	private InputStream resource;
	private String pathname;
	
	public ComputerFile() {};
	
	@ExposedToLua(value = false)
	public ComputerFile(String pathname, Computer computer) {
		this.computer = computer;
		this.pathname = pathname;
		if (!pathname.startsWith("/rom")) {
			this.javaFile = new File(computer.getBaseDirectory() + File.separator + pathname);
			
			try {
				if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
					throw new LuaRuntimeException("Error! Not allowed to open files outside of its base directory!");
				}
			} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }

			if (pathname.contains(".persist")) {
				throw new LuaRuntimeException("Error! Not allowed to open the persist file!");
			}
			
			if (!this.exists()) {
				if (computer.getBaseDirectory().length() > ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10) * 1024 * 1024) {
					throw new LuaRuntimeException("Error! Tried to make a new file when the disk is full!");
				}
			}
			
			if (computer.getNumberOfOpenFileHandles() >= computer.getMaxFileHandles()) {
				throw new LuaRuntimeException("Error! Tried to open too many files!");
			} else {
				computer.incrementOpenFileHandles();
			}
		} else {
			resource = this.getClass().getResourceAsStream("/info/cerios/electrocraft" + pathname);
		}
		
		if (resource == null && javaFile == null) {
			throw new LuaRuntimeException("Error making handle");
		}
	}
	
	@ExposedToLua
	public boolean mkdirs() {
		if (resource != null)
			return false;
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				throw new LuaRuntimeException("Error! Not allowed to open files outside of its base directory!");
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		
		return javaFile.mkdirs();
	}
	
	@ExposedToLua
	public boolean mkdir() {
		if (resource != null)
			return false;
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		
		return javaFile.mkdir();
	}
	
	@ExposedToLua
	public boolean createNewFile() throws IOException {
		if (resource != null)
			return false;
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return javaFile.createNewFile();
	}
	
	@ExposedToLua
	public boolean canWrite() {
		if (resource != null)
			return false;
		if (computer.getBaseDirectory().length() > ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10) * 1024 * 1024) {
			return false;
		}
		return javaFile.canWrite();
	}
	
	@ExposedToLua
	public boolean canRead() {
		if (resource != null)
			return true;
		return javaFile.canRead();
	}
	
	@ExposedToLua
	public boolean delete() {
		if (resource != null)
			return false;
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return javaFile.delete();
	}
	
	@ExposedToLua
	public boolean exists() {
		if (resource != null)
			return true;
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
				return false;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return javaFile.exists();
	}
	
	@ExposedToLua
	public long getFreeSpace()  {
		if (resource != null)
			return 0;
		return (ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10) * 1024 * 1024) - computer.getBaseDirectory().length();
	}
	
	@ExposedToLua
	public String getPath() {
		return pathname;
	}
	
	@ExposedToLua
	public String getName() {
		if (resource != null)
			return pathname.substring(pathname.lastIndexOf("/"));
		return javaFile.getName();
	}
	
	@ExposedToLua
	public ComputerFile getParentFile() {
		if (resource != null)
			return new ComputerFile(pathname.endsWith("/") ? pathname.substring(0, pathname.length() - 1).substring(0, pathname.substring(0, pathname.length() - 1).lastIndexOf("/")) : pathname.substring(0, pathname.lastIndexOf("/")), computer);
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile.getParentFile())) {
				return null;
			}
		} catch (IOException e) { throw new LuaRuntimeException("Error checking if path is valid"); }
		return new ComputerFile(javaFile.getParent(), computer);
	}
	
	@ExposedToLua
	public boolean isFile() {
		if (resource != null)
			return true;
		return javaFile.isFile();
	}
	
	@ExposedToLua
	public boolean isDirectory() {
		if (resource != null)
			return false;
		return javaFile.isDirectory();
	}
	
	@ExposedToLua
	public long length() {
		if (resource != null)
			try {
				return resource.available();
			} catch (IOException e) {
				return 0;
			}
		return javaFile.length();
	}
	
	@ExposedToLua
	public ComputerFile[] listFiles() {
		if (resource != null)
			return null;
		try {
			if (!Utils.baseDirectoryContains(computer.getBaseDirectory(), javaFile)) {
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
	
	@ExposedToLua
	public void close() {
		javaFile = null;
		resource = null;
		computer.deincrementOpenFileHandles();
	}
	
	@ExposedToLua
	public Object open(String mode) throws IOException {
		if (resource != null) {
			if (mode.equalsIgnoreCase("r")) {
				return new ComputerBufferedReader(new InputStreamReader(resource));
			} else if (mode.equalsIgnoreCase("rb")) {
				return new ComputerInputStream(resource);
			}
		} else if (mode.equalsIgnoreCase("r")) {
			return new ComputerBufferedReader(new FileReader(javaFile));
		} else if (mode.equalsIgnoreCase("w")) {
			return new ComputerBufferedWriter(new FileWriter(javaFile));
		} else if (mode.equalsIgnoreCase("a")) {
			return new ComputerBufferedWriter(new FileWriter(javaFile, true));
		} else if (mode.equalsIgnoreCase("rb")) {
			return new ComputerInputStream(new FileInputStream(javaFile));
		} else if (mode.equalsIgnoreCase("wb")) {
			return new ComputerOutputStream(new FileOutputStream(javaFile));
		} else if (mode.equalsIgnoreCase("ab")) {
			return new ComputerOutputStream(new FileOutputStream(javaFile, true));
		}
		return null;
	}
	
	@ExposedToLua(value = false)
	public void finalize() throws Throwable {
		if (javaFile != null)
			computer.deincrementOpenFileHandles();
		super.finalize();
	}
	
	@ExposedToLua(value = false)
	public File getJavaFile() {
		return javaFile;
	}
	
	@ExposedToLua(value = false)
	public InputStream getResource() {
		return resource;
	}
	
	@ExposedToLua(value = false)
	@Override
	public NamedJavaFunction[] getGlobalFunctions(Computer computer) {
		return new NamedJavaFunction[] {
				new NamedJavaFunction() {
					Computer computer;

					public NamedJavaFunction init(Computer computer) {
						this.computer = computer;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						try {
							ComputerFile file = new ComputerFile(luaState.checkString(-1), computer);
							luaState.pushJavaObject(file);
						} catch (LuaRuntimeException e) {
							luaState.pushNil();
						}
						return 1;
					}

					@Override
					public String getName() {
						return "createNewFileHandle";
					}
				}.init(computer)
		};
	}
	
	@ExposedToLua(value = false)
	@Override
	public String getNamespace() {
		return "file";
	}
	
	@ExposedToLua(value = false)
	@Override
	public void tick(Computer computer) {
	}
	
	@ExposedToLua(allowAll = true)
	public class ComputerBufferedReader extends BufferedReader {
		public ComputerBufferedReader(Reader in) {
			super(in);
		}
		
		public String readAll() throws IOException {
			StringBuilder result = new StringBuilder(new String());
			String line = readLine();
			while(line != null) {
				result.append(line);
				line = readLine();
				if( line != null ) {
					result.append("\n");
				}
			}
			return result.toString();
		}
	}
	
	@ExposedToLua(allowAll = true)
	public class ComputerBufferedWriter extends BufferedWriter {
		public ComputerBufferedWriter(Writer arg0) {
			super(arg0);
		}
	}
	
	@ExposedToLua(allowAll = true)
	public class ComputerInputStream extends DataInputStream {
		public ComputerInputStream(InputStream stream) {
			super(stream);
		}
	}
	
	@ExposedToLua(allowAll = true)
	public class ComputerOutputStream extends DataOutputStream {
		public ComputerOutputStream(OutputStream stream) {
			super(stream);
		}
	}
}
