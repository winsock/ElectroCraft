package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import info.cerios.electrocraft.core.electricity.ElectricityProvider;
import info.cerios.electrocraft.core.electricity.ElectricityReceiver;
import info.cerios.electrocraft.core.electricity.ElectricityTransporter;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class TileEntityElectroWire extends ElectricBlock implements ElectricityTransporter {
	@Override
	public float getMaxCurrent() {
		return 0.005f; // 5mA
	}

	@Override
	public float getResistance() {
		return 0.5f; // 0.5 ohms
	}
	
	@Override
	public boolean canConnect(ElectricBlock block) {
		return block.getBlockType().blockID == ElectroBlocks.ELECTRO_WIRE.getBlock().blockID || block instanceof ElectricityReceiver || block instanceof ElectricityProvider;
	}
}
