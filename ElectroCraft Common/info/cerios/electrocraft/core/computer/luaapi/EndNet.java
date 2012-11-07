package info.cerios.electrocraft.core.computer.luaapi;

import info.cerios.electrocraft.api.computer.luaapi.LuaAPI;
import info.cerios.electrocraft.core.computer.Computer;

import java.util.Map;
import java.util.WeakHashMap;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

public class EndNet implements LuaAPI {

	private static Map<Integer, Computer> addressMap = new WeakHashMap<Integer, Computer>();

	@Override
	public String getNamespace() {
		return "enc";
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
					addressMap.get(luaState.checkInteger(-2))
							.postEvent("end", luaState.checkInteger(-2),
									luaState.checkString(-1));
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
