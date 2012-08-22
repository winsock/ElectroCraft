package info.cerios.electrocraft.core.computer;

import java.util.HashMap;
import java.util.Map;

@ExposedToLua
public class MinecraftIOInterface {
	
	private Map<Integer, NetworkBlock> ioMap = new HashMap<Integer, NetworkBlock>();
	
	@ExposedToLua(value = false)
	public MinecraftIOInterface() {}
	
	@ExposedToLua(value = false)
	public void register(NetworkBlock block) {
		ioMap.put(block.getControlAddress(), block);
		ioMap.put(block.getDataAddress(), block);
	}
}
