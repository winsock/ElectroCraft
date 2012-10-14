package info.cerios.electrocraft.core.blocks.tileentities;

import net.minecraftforge.common.ForgeDirection;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.NetworkBlock;

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
