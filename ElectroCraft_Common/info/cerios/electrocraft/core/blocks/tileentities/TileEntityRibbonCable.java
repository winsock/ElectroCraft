package info.cerios.electrocraft.core.blocks.tileentities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Orientations;

public class TileEntityRibbonCable extends NetworkBlock {

	@Override
	public boolean canConnectNetwork(NetworkBlock block) {
		return true;
	}

	@Override
	public boolean canConnect(ElectricBlock block) {
		return false;
	}

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
	public boolean initialised() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean updated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadState(DataInput input) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveState(DataOutput output) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
