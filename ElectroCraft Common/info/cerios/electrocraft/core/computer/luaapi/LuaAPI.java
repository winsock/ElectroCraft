package info.cerios.electrocraft.core.computer.luaapi;

import info.cerios.electrocraft.core.computer.Computer;

import com.naef.jnlua.NamedJavaFunction;

public interface LuaAPI {
	public NamedJavaFunction[] getGlobalFunctions(Computer computer);
}
