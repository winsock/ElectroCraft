package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import info.cerios.electrocraft.core.electricity.ElectricityTransporter;

public class TileEntityWire extends ElectricBlock implements ElectricityTransporter {

	@Override
	public float getMaxCurrent() {
		return 5; // 5 Amps
	}

	@Override
	public float getResistance() {
		return 0.05f; // 0.05 Ohms
	}

	@Override
	public boolean canConnect(ElectricBlock block) {
		return worldObj.getBlockId(xCoord, yCoord, zCoord) != ElectroBlocks.ELECTRO_WIRE.getBlock().blockID;
	}
}
