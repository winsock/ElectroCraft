package info.cerios.electrocraft.core.blocks.tileentities;

import net.minecraftforge.common.ForgeDirection;
import info.cerios.electrocraft.core.computer.NetworkBlock;

public class TileEntitySerialCable extends NetworkBlock {
	
	private boolean[] connectedBlocks = new boolean[6];
	
	public TileEntitySerialCable() {
		for (int i = 0; i < 6; i++) {
			connectedBlocks[i] = false;
		}
	}
	
	@Override
	public boolean canConnectNetwork(NetworkBlock block) {
		return true;
	}
	
	public void setBlockConnected(ForgeDirection direction) {
		connectedBlocks[direction.ordinal()] = true;
	}
	
	public boolean isBlockConnected(int side) {
		if (side > 5)
			side = 5;
		return connectedBlocks[side];
	}
	
	@Override
	public Object onTaskComplete(Object... objects) {
		return 0;
	}
}
