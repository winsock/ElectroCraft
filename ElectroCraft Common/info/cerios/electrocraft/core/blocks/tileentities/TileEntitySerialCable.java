package info.cerios.electrocraft.core.blocks.tileentities;

import net.minecraftforge.common.ForgeDirection;
import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.computer.Computer;

public class TileEntitySerialCable extends NetworkBlock {
		
	public TileEntitySerialCable() {
	}
	
	@Override
	public boolean canConnectNetwork(NetworkBlock block) {
		return true;
	}

	@Override
	public void tick(Computer computer) {
	}
}
