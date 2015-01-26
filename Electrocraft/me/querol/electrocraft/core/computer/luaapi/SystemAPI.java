package me.querol.electrocraft.core.computer.luaapi;

import me.querol.com.naef.jnlua.LuaState;
import me.querol.com.naef.jnlua.NamedJavaFunction;
import me.querol.electrocraft.api.computer.luaapi.LuaAPI;
import me.querol.electrocraft.core.computer.Computer;

/**
 * Created by winsock on 1/25/15.
 */
public class SystemAPI implements LuaAPI {
    @Override
    public String getNamespace() {
        return "sys";
    }

    @Override
    public NamedJavaFunction[] getGlobalFunctions(final Computer computer) {
        NamedJavaFunction getCurrentTime = new NamedJavaFunction() {
            @Override
            public String getName() {
                return "time";
            }

            @Override
            public int invoke(LuaState luaState) {
                luaState.pushNumber(java.lang.System.currentTimeMillis() / 1000);
                return 1;
            }
        };

        NamedJavaFunction getUptime = new NamedJavaFunction() {
            @Override
            public String getName() {
                return "uptime";
            }

            @Override
            public int invoke(LuaState luaState) {
                luaState.pushNumber((java.lang.System.currentTimeMillis() - computer.getStartTime()) / 1000);
                return 1;
            }
        };

        return new NamedJavaFunction[] { getCurrentTime, getUptime };
    }

    @Override
    public void tick(Computer computer) {

    }
}
