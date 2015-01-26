package me.querol.electrocraft.core.computer.luaapi;

import me.querol.electrocraft.api.computer.ExposedToLua;
import me.querol.electrocraft.api.computer.NetworkBlock;
import me.querol.electrocraft.api.computer.luaapi.LuaAPI;
import me.querol.electrocraft.core.computer.Computer;

import java.util.HashMap;
import java.util.Map;

import me.querol.com.naef.jnlua.LuaState;
import me.querol.com.naef.jnlua.NamedJavaFunction;

@ExposedToLua
public class MinecraftInterface implements LuaAPI {

	private Map<Integer, NetworkBlock> ioMap = new HashMap<Integer, NetworkBlock>();

	@ExposedToLua(value = false)
	public MinecraftInterface() {
	}

	@ExposedToLua(value = false)
	public void register(NetworkBlock block) {
		ioMap.put(block.getControlAddress(), block);
		ioMap.put(block.getDataAddress(), block);
	}

	@ExposedToLua(value = false)
	public void remove(NetworkBlock block) {
		ioMap.remove(block.getControlAddress());
		ioMap.remove(block.getDataAddress());
	}

	@ExposedToLua(value = false)
	@Override
	public NamedJavaFunction[] getGlobalFunctions(Computer computer) {
		return new NamedJavaFunction[] { new NamedJavaFunction() {
			public NamedJavaFunction init(Computer computer) {
				return this;
			}

			@Override
			public int invoke(LuaState luaState) {
				if (ioMap.containsKey(luaState.checkInteger(-1))) {
					luaState.pushJavaObject(ioMap.get(luaState.checkInteger(-1)));
					return 1;
				}
				return 0;
			}

			@Override
			public String getName() {
				return "wrapDevice";
			}
		}.init(computer) };
	}

	@Override
	public String getNamespace() {
		return "mc";
	}

	@Override
	public void tick(Computer computer) {
		for (NetworkBlock block : ioMap.values()) {
			block.tick(computer);
		}
	}
}
