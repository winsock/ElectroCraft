package me.querol.electrocraft.core.computer.luaapi;

import me.querol.electrocraft.api.computer.luaapi.LuaAPI;
import me.querol.electrocraft.core.computer.Computer;

import java.util.Map;
import java.util.WeakHashMap;

import me.querol.com.naef.jnlua.LuaState;
import me.querol.com.naef.jnlua.NamedJavaFunction;

public class EndNet implements LuaAPI {

	private static Map<Integer, Computer> addressMap = new WeakHashMap<Integer, Computer>();

	@Override
	public String getNamespace() {
		return "endnet";
	}

	@Override
	public NamedJavaFunction[] getGlobalFunctions(Computer computer) {
		return new NamedJavaFunction[] { new NamedJavaFunction() {
			Computer computer;

			public NamedJavaFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}

			@Override
			public int invoke(LuaState luaState) {
				if (!addressMap.containsKey(luaState.checkInteger(-1))) {
					addressMap.put(luaState.checkInteger(-1), computer);
				}
				return 0;
			}

			@Override
			public String getName() {
				return "bind";
			}
		}.init(computer), new NamedJavaFunction() {
			public NamedJavaFunction init(Computer computer) {
				return this;
			}

			@Override
			public int invoke(LuaState luaState) {
				if (addressMap.containsKey(luaState.checkInteger(-2))) {
					//addressMap.get(luaState.checkInteger(-2)).postEvent("end", luaState.checkInteger(-2), luaState.checkString(-1));
				}
				return 0;
			}

			@Override
			public String getName() {
				return "send";
			}
		}.init(computer) };
	}

	@Override
	public void tick(Computer computer) {
	}
}
