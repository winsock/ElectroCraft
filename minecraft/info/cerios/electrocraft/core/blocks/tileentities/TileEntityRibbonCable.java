package info.cerios.electrocraft.core.blocks.tileentities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.cerios.electrocraft.core.computer.ComputerHandler;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.electricity.ElectricBlock;

public class TileEntityRibbonCable extends NetworkBlock {

	@Override
	public void ioPortWriteByte(int address, int data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ioPortWriteWord(int address, int data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ioPortWriteLong(int address, int data) {
		// TODO Auto-generated method stub

	}

	@Override
	public int ioPortReadByte(int address) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int ioPortReadWord(int address) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int ioPortReadLong(int address) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void loadState(DataInput input) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveState(DataOutput output) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canConnectNetwork(NetworkBlock block) {
		return true;
	}

	@Override
	public boolean canConnect(ElectricBlock block) {
		return false;
	}

}
