package me.querol.electrocraft.api.computer.luaapi;

import me.querol.electrocraft.core.computer.Computer;

import me.querol.com.naef.jnlua.NamedJavaFunction;

public interface LuaAPI {
    public String getNamespace();

    public NamedJavaFunction[] getGlobalFunctions(Computer computer);

    public void tick(Computer computer);
}
