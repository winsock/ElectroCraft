package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.ComputerServerClient;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.src.EntityPlayerMP;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaState.GcAction;

import cpw.mods.fml.relauncher.RelaunchClassLoader;

public class LuaSecurity extends SecurityManager {
	
	private final String key;
	private final InheritableThreadLocal<Computer> threadLocal = new InheritableThreadLocal<Computer>();
	private final ThreadGroup threadGroup = new ThreadGroup("luaThreads");
	
	public LuaSecurity(String key) {
		super();
		this.key = key;
	}
	
	public void registerThread(Computer computer) {		
		// Checker thread
		new Thread(new Runnable() {
			Computer computer;
			
			public Runnable init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public void run() {
				while (computer.isRunning()) {
					synchronized(Computer.luaStateLock) {
						// System memory check
						if (computer.getLuaState().gc(GcAction.COUNT, 0) > ConfigHandler.getCurrentConfig().get("computer", "MaxMemPerUser", 16).getInt(16) * 1024) {
							computer.setRunning(false);
							computer.getTerminal().print("ERROR: Ran out of memory! Max memory is: " + String.valueOf(ConfigHandler.getCurrentConfig().get("computer", "MaxMemPerUser", 16).getInt(16)) + "M");
						}

						// Extra backup check in case my wrapped file manager doesn't catch it
						if (computer.getBaseDirectory().length() > ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10) * 1024 * 1024) {
							computer.setRunning(false);
							computer.getTerminal().print("ERROR: Ran out of storage! Max storage space is: " + String.valueOf(ConfigHandler.getCurrentConfig().get("computer", "MaxStoragePerUser", 10).getInt(10)) + "M");
						}
					}

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) { }
				}
			}
		}.init(computer)).start();
		threadLocal.set(computer);
	}
	
	public boolean shouldCheckPermissions() {
		for (Class c : this.getClassContext()) {
			if (c == RelaunchClassLoader.class)
				return false;
			if (c == LuaState.class)
				return false;
			if (c == Utils.class)
				return false;
		}
		return threadLocal.get() != null;
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
	public void checkWrite(FileDescriptor fd) {
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to open raw file descriptors!");
	}
	
	@Override
	public void checkWrite(String file) {
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
	public void checkDelete(String file) {
		if (!shouldCheckPermissions())
			return;
		try {
			if (!Utils.baseDirectoryContains(threadLocal.get().getBaseDirectory(), new File(file))) {
				throw new SecurityException("Error! Lua not allowed to delete files outside of its base directory!");
			}
		} catch (IOException e) {
			throw new SecurityException("Error! Lua is not allowed to delete this file!");
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
		if (pkg.equals("cpw.mods.fml.relauncher") || !shouldCheckPermissions())
			return;
		
		if (!pkg.startsWith("java") && !pkg.startsWith("info.cerios.electrocraft.core.computer")) {
			throw new SecurityException("Error! Lua not allowed to access non java.* or info.cerios.electrocraft.core.computer.* packages!");
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
	
	@Override
	public void checkAccess(Thread thread) {
		if (!shouldCheckPermissions())
			return;
		if (!threadGroup.parentOf(thread.getThreadGroup())) {
			throw new SecurityException("Error! Lua not allowed to access other threads than itself or subthreads!");
		}
	}
	
	public ThreadGroup getThreadGroup() {
		return threadGroup;
	}
}
