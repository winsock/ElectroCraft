package info.cerios.electrocraft.core.blocks.tileentities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.cerios.electrocraft.core.computer.ComputerHandler;
import info.cerios.electrocraft.core.computer.IOPortCapableMinecraft;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.electricity.ElectricBlock;

public class TileEntityRedstoneAdapter extends NetworkBlock {

	private boolean redstonePower = false;
	private boolean receiveMode = false;
	
	public TileEntityRedstoneAdapter() {
		this.dataAddress = 225;
		this.controlAddress = 226;
	}
	
	public boolean getRedstonePower() {
		return redstonePower;
	}

	@Override
	public void ioPortWriteByte(int address, int data) {
		if (address == dataAddress) {
			if (data > 0)
				redstonePower = true;
			else if (data <= 0)
				redstonePower = false;
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 15);
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, this.getBlockType().blockID);
		} else if (address == controlAddress) {
			if (data > 0)
				receiveMode = true;
			else if (data <= 0)
				receiveMode = false;
		}
	}

	@Override
	public void ioPortWriteWord(int address, int data) {
	}

	@Override
	public void ioPortWriteLong(int address, int data) {
	}

	@Override
	public int ioPortReadByte(int address) {
		if (address == dataAddress) {
			if (receiveMode) {
				return this.worldObj.isBlockGettingPowered(xCoord, yCoord, zCoord) ? 1 : 0;
			} else {
				return redstonePower ? 1 : 0;
			}
		}
		return -1;
	}

	@Override
	public int ioPortReadWord(int address) {
		return 0;
	}

	@Override
	public int ioPortReadLong(int address) {
		return 0;
	}

	@Override
	public void loadState(DataInput input) throws IOException {
	}

	@Override
	public void saveState(DataOutput output) throws IOException {
	}

	@Override
	public boolean canConnect(ElectricBlock block) {
		return true;
	}

	@Override
	public boolean canConnectNetwork(NetworkBlock block) {
		return true;
	}
}
