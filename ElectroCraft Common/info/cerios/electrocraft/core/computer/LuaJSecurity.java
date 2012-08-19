package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.network.ComputerServerClient;
import info.cerios.electrocraft.core.utils.Utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.security.Permission;

public class LuaJSecurity extends SecurityManager {
	
	private final String key;
	private final InheritableThreadLocal<Computer> threadLocal = new InheritableThreadLocal<Computer>();
	
	public LuaJSecurity(String key) {
		this.key = key;
	}
	
	public void registerThread(Computer computer) {
		threadLocal.set(computer);
	}
	
	public boolean shouldCheckPermissions() {
		assert threadLocal.get() != null;
		return threadLocal.get() != null;
	}
	
	@Override
    public void checkPermission(Permission perm) {
		super.checkPermission(perm);
		if (!shouldCheckPermissions())
			return;
	}
	
	@Override
	public void checkExec(String cmd) {
		super.checkExec(cmd);
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to create subprocesses!");
	}
	
	@Override
	public void checkLink(String lib) {
		super.checkLink(lib);
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to open native libraries!");
	}
	
	@Override
	public void checkRead(FileDescriptor fd) {
		super.checkRead(fd);
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to open raw file descriptors!");
	}
	
	public void checkRead(String file, Object context) {
		super.checkRead(file, context);
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
		super.checkRead(file);
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
		super.checkPropertiesAccess();
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to access or change system properties!");
	}
	
	@Override
	public void checkExit(int status) {
		super.checkExit(status);
		if (!shouldCheckPermissions())
			return;
		throw new SecurityException("Error! Lua not allowed to exit the JVM!");
	}
	
	@Override
	public void checkPackageAccess(String pkg) {
		super.checkPackageAccess(pkg);
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
	
	@Override
	public void checkMemberAccess(Class clazz, int which) {
		
	}
}
