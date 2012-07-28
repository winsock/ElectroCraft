package info.cerios.electrocraft.core.blocks.tileentities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import info.cerios.electrocraft.core.computer.ComputerHandler;
import info.cerios.electrocraft.core.computer.IOPortCapableMinecraft;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import info.cerios.electrocraft.core.electricity.ElectricityProvider;
import info.cerios.electrocraft.core.electricity.ElectricityTypes;

public class TileEntityStaticGenerator extends IOPortCapableMinecraft implements ElectricityProvider {

	private int currentVoltage = 0;
	private int outputSetting = 0;
	
	public TileEntityStaticGenerator(ComputerHandler computerHandler) {
		super(computerHandler);
	}

	@Override
	public int getVoltage() {
		return currentVoltage;
	}

	@Override
	public float getCurrent() {
		return .001f;
	}

	@Override
	public boolean canConnect(ElectricBlock block) {
		return true; // Can connect to any electric block
	}

	@Override
	public ElectricityTypes getTypeOfCurrent() {
		return ElectricityTypes.VC;
	}

	@Override
	public void ioPortWriteByte(int address, int data) {
		if (address == 227) {
			outputSetting = data;
		} else if (address == 228) {
			if (data == 0) {
				currentVoltage = 0;
			} else if (data > 0) {
				if (data > 5)
					currentVoltage = 5;
				else
					currentVoltage = data;
			}
			// Recompute the power network
			this.update(this);
		}
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
		if (outputSetting == 0) {
		return getVoltage();
		} else if (outputSetting == 1) {
			return Float.floatToRawIntBits(getCurrent());
		}
		return -1;
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
	public int[] ioPortsRequested() {
		return new int[] { 227, 228 };
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
