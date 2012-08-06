package info.cerios.electrocraft.core.blocks.tileentities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Orientations;

public class TileEntityRibbonCable extends NetworkBlock {
	@Override
	public boolean canConnectNetwork(NetworkBlock block) {
		return true;
	}
}
