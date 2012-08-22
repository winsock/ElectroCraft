package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.ComputerServerClient;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.security.Permission;
import java.util.logging.Level;

import net.minecraft.src.EntityPlayerMP;

import com.naef.jnlua.LuaState.GcAction;

public class LuaSecurity extends SecurityManager {
	
	private final String key;
	private final InheritableThreadLocal<Computer> threadLocal = new InheritableThreadLocal<Computer>();
	
	public LuaSecurity(String key) {
		super();
		this.key = key;
	}
	
	public void registerThread(Computer computer) {
		threadLocal.set(computer);
		
		// Memory check thread
		new Thread(new Runnable() {
			Computer computer;
			
			public Runnable init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public void run() {
				while (computer.isRunning()) {
					if (computer.getLuaState().gc(GcAction.COUNT, 0) > ConfigHandler.getCurrentConfig().getOrCreateIntProperty("MaxMemPerUser", "computer", 16).getInt(16) * 1024) {
						computer.getTerminal().print("ERROR: Ran out of memory! Max memory is: " + String.valueOf(ConfigHandler.getCurrentConfig().getOrCreateIntProperty("MaxMemPerUser", "computer", 16).getInt(16)) + "M");
						computer.setRunning(false);
						ElectroCraft.instance.getLogger().log(Level.WARNING, "Warning: Player: " + ((EntityPlayerMP)computer.getClient().getComputer().getActivePlayer()).username + " exceded the maximum memory allowance");
					}
				}
			}
		}.init(computer)).start();
	}
	
	public boolean shouldCheckPermissions() {
		return false;
	}
	
	@Override
    public void checkPermission(Permission perm) {
		if (!shouldCheckPermissions())
			return;
		super.checkPermission(perm);
	}
	
	@Override
	public void checkExec(String cmd) {
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to create subprocesses!");
	}
	
	@Override
	public void checkLink(String lib) {
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to open native libraries!");
	}
	
	@Override
	public void checkRead(FileDescriptor fd) {
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to open raw file descriptors!");
	}
	
	public void checkRead(String file, Object context) {
		if (!shouldCheckPermissions())
			return;
		try {
			if (!Utils.baseDirectoryContains(threadLocal.get().getBaseDirectory(), new File(file))) {
				throw new SecurityException("Error! Lua not allowed to open files outside of its base directory!");
			}
		} catch (IOException e) {
			throw new SecurityException("Error! Lua is not allowed to open this file!");
		}
	}
	
	@Override
	public void checkRead(String file) {
		if (!shouldCheckPermissions())
			return;
		try {
			if (!Utils.baseDirectoryContains(threadLocal.get().getBaseDirectory(), new File(file))) {
				throw new SecurityException("Error! Lua not allowed to open files outside of its base directory!");
			}
		} catch (IOException e) {
			throw new SecurityException("Error! Lua is not allowed to open this file!");
		}
	}
	
	@Override
	public void checkPropertiesAccess() {
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to access or change system properties!");
	}
	
	@Override
	public void checkExit(int status) {
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to exit the JVM!");
	}
	
	@Override
	public void checkPackageAccess(String pkg) {
		if (!shouldCheckPermissions())
			return;
		
		if (!pkg.startsWith("java") && !pkg.startsWith("info.cerios.electrocraft.core.computer.luajavaapi")) {
			throw new SecurityException("Error! Lua not allowed to access non java.* or info.cerios.electrocraft.core.computer.luajavaapi.* packages!");
		}
		
		if (pkg.startsWith("java.lang.reflect")) {
			throw new SecurityException("Error! Lua not allowed to access java.lang.reflect.* packages!");
		}
		
		if (pkg.startsWith("java.util.prefs")) {
			throw new SecurityException("Error! Lua not allowed to access java.util.prefs.* packages!");
		}
		
		if (pkg.startsWith("java.util.jar")) {
			throw new SecurityException("Error! Lua not allowed to access java.util.jar.* packages!");
		}
		
		if (pkg.startsWith("java.lang.ref")) {
			throw new SecurityException("Error! Lua not allowed to access java.util.jar.* packages!");
		}
		
		if (pkg.startsWith("java.security")) {
			throw new SecurityException("Error! Lua not allowed to access java.security.* packages!");
		}
		
		if (pkg.startsWith("java.awt")) {
			throw new SecurityException("Error! Lua not allowed to access java.awt.* packages!");
		}
	}
}
