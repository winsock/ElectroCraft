package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.core.computer.NetworkBlock;

public class TileEntityRibbonCable extends NetworkBlock {
    @Override
    public boolean canConnectNetwork(NetworkBlock block) {
        return true;
    }

	@Override
	public Object onTaskComplete(Object... objects) {
		return 0;
	}
}
