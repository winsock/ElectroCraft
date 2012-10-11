package info.cerios.electrocraft.core.computer.luaapi;

import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.ExposedToLua;
import info.cerios.electrocraft.core.computer.NetworkBlock;

import java.util.HashMap;
import java.util.Map;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

@ExposedToLua
public class MinecraftIOInterface implements LuaAPI {
	
	private Map<Integer, NetworkBlock> ioMap = new HashMap<Integer, NetworkBlock>();
	
	@ExposedToLua(value = false)
	public MinecraftIOInterface() {}
	
	@ExposedToLua(value = false)
	public void register(NetworkBlock block) {
		ioMap.put(block.getControlAddress(), block);
		ioMap.put(block.getDataAddress(), block);
	}
	
	@ExposedToLua(value = false)
	@Override
	public NamedJavaFunction[] getGlobalFunctions(Computer computer) {
		return new NamedJavaFunction[] {
				new NamedJavaFunction() {
					Computer computer;
					
					public NamedJavaFunction init(Computer computer) {
						this.computer = computer;
						return this;
					}
					
					@Override
					public int invoke(LuaState luaState) {
						if (ioMap.containsKey(luaState.checkInteger(0))) {
							luaState.pushJavaObject(new WrappedDevice(ioMap.get(luaState.checkInteger(0))));
							return 1;
						}
						return 0;
					}

					@Override
					public String getName() {
						return "wrapDevice";
					}
				}.init(computer)
		};
	}
	
	private class WrappedDevice {
		public WrappedDevice(NetworkBlock device) {
			
		}
	}
}
