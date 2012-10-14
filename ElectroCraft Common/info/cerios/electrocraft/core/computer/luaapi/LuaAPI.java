package info.cerios.electrocraft.core.computer.luaapi;

import info.cerios.electrocraft.core.computer.Computer;

import com.naef.jnlua.NamedJavaFunction;

public interface LuaAPI {
	public String getNamespace();
	public NamedJavaFunction[] getGlobalFunctions(Computer computer);
	public void tick(Computer computer);
}
