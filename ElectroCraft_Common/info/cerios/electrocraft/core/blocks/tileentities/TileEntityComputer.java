package info.cerios.electrocraft.core.blocks.tileentities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

import info.cerios.electrocraft.core.AbstractElectroCraftMod;
import info.cerios.electrocraft.core.computer.IComputer;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import info.cerios.electrocraft.core.electricity.ElectricityReceiver;
import info.cerios.electrocraft.core.utils.ObjectTriplet;

public class TileEntityComputer extends NetworkBlock implements ElectricityReceiver {

	private IComputer computer;
	private boolean hasRegistered = false;
	private Set<ObjectTriplet<Integer, Integer, Integer>> ioPorts = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
	
	public TileEntityComputer() {
		this.controlAddress = 223;
		this.dataAddress = 224;
		ioPorts.add(new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord, zCoord));
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		NBTTagList ioPortList = new NBTTagList("ioPorts");
		for (ObjectTriplet<Integer, Integer, Integer> ioPort: ioPorts) {
			NBTTagCompound ioPortData = new NBTTagCompound("ioPortData");
			ioPortData.setInteger("x", ioPort.getValue1());
			ioPortData.setInteger("y", ioPort.getValue2());
			ioPortData.setInteger("z", ioPort.getValue3());
			ioPortList.appendTag(ioPortData);
		}
		nbttagcompound.setTag("ioPorts", ioPortList);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		NBTTagList ioPortList = nbttagcompound.getTagList("ioPorts");
		for (int i = 0; i < ioPortList.tagCount(); i++) {
			if (ioPortList.tagAt(i) instanceof NBTTagCompound) {
				NBTTagCompound ioPortData = (NBTTagCompound) ioPortList.tagAt(i);
				ioPorts.add(new ObjectTriplet<Integer, Integer, Integer>(ioPortData.getInteger("x"), ioPortData.getInteger("y"), ioPortData.getInteger("z")));
			}
		}
		hasRegistered = false;
	}
	
	public IComputer getComputer() {
		return computer;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!hasRegistered) {
			reRegisterIOPorts();
			hasRegistered = true;
		}
	}
	
	public void registerIOPort(NetworkBlock ioPort) {
		ioPorts.add(new ObjectTriplet<Integer, Integer, Integer>(ioPort.xCoord, ioPort.yCoord, ioPort.zCoord));
		if (computer != null)
			computer.addPart(ioPort);
	}
	
	public void removeIOPort(NetworkBlock ioPort) {
		ioPorts.remove(new ObjectTriplet<Integer, Integer, Integer>(ioPort.xCoord, ioPort.yCoord, ioPort.zCoord));
		if (computer != null)
			computer.removePart(ioPort);
	}
	
	public void reRegisterIOPorts() {
		if (computer == null)
			return;
		for (ObjectTriplet<Integer, Integer, Integer> ioPort: ioPorts) {
			if (this.getNetworkBlockFromLocation(ioPort.getValue1(), ioPort.getValue2(), ioPort.getValue3()) == null)
				continue;
			computer.removePart(this.getNetworkBlockFromLocation(ioPort.getValue1(), ioPort.getValue2(), ioPort.getValue3()));
			computer.addPart(this.getNetworkBlockFromLocation(ioPort.getValue1(), ioPort.getValue2(), ioPort.getValue3()));
		}
	}

	public void setComputer(IComputer computer) {
		if (this.computer != null)
			AbstractElectroCraftMod.getInstance().getComputerHandler().stopComputer(this.computer);
		this.computer = computer;
		reRegisterIOPorts();
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
				// TODO Send close GUI packet
			} else if (data == 2) {
				AbstractElectroCraftMod.getInstance().getComputerHandler().stopComputer(computer);
				reRegisterIOPorts();
			} else if (data == 3) {
				reRegisterIOPorts();
				AbstractElectroCraftMod.getInstance().getComputerHandler().resetComputer(computer);
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
	public boolean canConnectNetwork(NetworkBlock block) {
		return true;
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
