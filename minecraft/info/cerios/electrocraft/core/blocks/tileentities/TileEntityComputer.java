package info.cerios.electrocraft.core.blocks.tileentities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.src.ModLoader;

import info.cerios.electrocraft.core.computer.ComputerHandler;
import info.cerios.electrocraft.core.computer.IOPortCapableMinecraft;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import info.cerios.electrocraft.core.electricity.ElectricityReceiver;
import info.cerios.electrocraft.core.jpc.emulator.PC;

public class TileEntityComputer extends NetworkBlock implements ElectricityReceiver {

	private PC computer;
	
	public TileEntityComputer() {
		this.controlAddress = 223;
		this.dataAddress = 224;
	}
	
	public PC getComputer() {
		if (computer == null)
			computer = computerHandler.createAndStartCompuer(this);
		return computer;
	}

	@Override
	public int getRequiredVoltage() {
		return 5;
	}

	@Override
	public float getCurrentDraw() {
		return .0005f;
	}

	@Override
	public boolean isOn() {
		return false;
	}

	@Override
	public int getMaxVoltage() {
		return 12;
	}

	@Override
	public boolean canConnect(ElectricBlock block) {
		return true;
	}

	@Override
	public void ioPortWriteByte(int address, int data) {
		if (address == dataAddress) {
			if (data == 1) {
				// Close out of the computer monitor screen
				ModLoader.getMinecraftInstance().displayGuiScreen(null);
			}
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
}
