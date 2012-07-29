package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRibbonCable;
import info.cerios.electrocraft.core.jpc.emulator.PC;
import info.cerios.electrocraft.core.utils.ObjectTriplet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

public class ComputerNetwork {
	protected Set<ObjectTriplet<Integer, Integer, Integer>> devices;
	protected Map<ObjectTriplet<Integer, Integer, Integer>, Boolean> probeStatus = new HashMap<ObjectTriplet<Integer, Integer, Integer>, Boolean>();
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		NBTTagList computerList = new NBTTagList("devices");
		if (devices != null) {
			for (ObjectTriplet<Integer, Integer, Integer> computer : devices) {
				NBTTagCompound computerData = new NBTTagCompound("deviceData");
				computerData.setInteger("x", computer.getValue1());
				computerData.setInteger("y", computer.getValue2());
				computerData.setInteger("z", computer.getValue3());
				computerList.appendTag(computerData);
			}
		}
		nbttagcompound.setTag("devices", computerList);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		NBTTagList computerList = nbttagcompound.getTagList("devices");
		for (int i = 0; i < computerList.tagCount(); i++) {
			if (computerList.tagAt(i) instanceof NBTTagCompound) {
				NBTTagCompound computerData = (NBTTagCompound) computerList.tagAt(i);
				int x = computerData.getInteger("x");
				int y = computerData.getInteger("y");
				int z = computerData.getInteger("z");
				
				if (devices == null)
					devices = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
				devices.add(new ObjectTriplet<Integer, Integer, Integer>(x, y, z));
			}
		}
	}
	
	public void updateProviderChain(NetworkBlock startBlock) {
		devices = getDevicesInChain(new ObjectTriplet<Integer, Integer, Integer>(startBlock.xCoord, startBlock.yCoord, startBlock.zCoord));
	}
	
	public void mergeNetwork(ComputerNetwork network) {
		devices.addAll(network.devices);
	}
	
	public Set<TileEntityComputer> getComputers() {
		Set<TileEntityComputer> tempSet = new HashSet<TileEntityComputer>();
		for (ObjectTriplet<Integer, Integer, Integer> computer : devices) {
			TileEntityComputer computerTileEntity = getTileEntityComputerFromLocation(computer.getValue1(), computer.getValue2(), computer.getValue3());
			if (computerTileEntity == null)
				break;
			tempSet.add(computerTileEntity);
		}
		return tempSet;
	}
	
	public void registerIOPort(IOPortCapableMinecraft ioPort) {
		for (ObjectTriplet<Integer, Integer, Integer> computer : devices) {
			TileEntityComputer computerTileEntity = getTileEntityComputerFromLocation(computer.getValue1(), computer.getValue2(), computer.getValue3());
			if (computerTileEntity == null)
				break;
			computerTileEntity.getComputer().addPart(ioPort);
		}
	}
	
	public void removeIOPort(IOPortCapableMinecraft ioPort) {
		for (ObjectTriplet<Integer, Integer, Integer> computer : devices) {
			TileEntityComputer computerTileEntity = getTileEntityComputerFromLocation(computer.getValue1(), computer.getValue2(), computer.getValue3());
			if (computerTileEntity == null)
				break;
			computerTileEntity.getComputer().removePart(ioPort);
		}
	}
	
	public void resetComputers() {
		for (ObjectTriplet<Integer, Integer, Integer> computer : devices) {
			TileEntityComputer computerTileEntity = getTileEntityComputerFromLocation(computer.getValue1(), computer.getValue2(), computer.getValue3());
			if (computerTileEntity == null)
				break;
			computerTileEntity.reset();
		}
	}
	
	public TileEntityComputer getTileEntityComputerFromLocation(int x, int y, int z) {
		if (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z) instanceof TileEntityComputer) {
			return (TileEntityComputer) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z);
		}
		return null;
	}
	
	public NetworkBlock getNetworkBlockFromLocation(int x, int y, int z) {
		if (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
			return (NetworkBlock) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z);
		}
		return null;
	}

	private Set<ObjectTriplet<Integer, Integer, Integer>> getDevicesInChain(ObjectTriplet<Integer, Integer, Integer> block) {
		if (probeStatus.get(block) != null && probeStatus.get(block))
			return null;
		probeStatus.put(block, true);
		
		// Check if the block is a device
		Set<ObjectTriplet<Integer, Integer, Integer>> devices = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
		if (getNetworkBlockFromLocation(block.getValue1(), block.getValue2(), block.getValue3()) != null)
			if (!(getNetworkBlockFromLocation(block.getValue1(), block.getValue2(), block.getValue3()) instanceof TileEntityRibbonCable))
				devices.add(block);
		else
			return null;
		
		// Check if any connected blocks are devices
		for (ObjectTriplet<Integer, Integer, Integer> nextBlock : getNetworkBlockFromLocation(block.getValue1(), block.getValue2(), block.getValue3()).connectedDevices.values()) {
			// Exit recursion when device found
			if (getNetworkBlockFromLocation(nextBlock.getValue1(), nextBlock.getValue2(), nextBlock.getValue3()) != null) {
				if (!(getNetworkBlockFromLocation(block.getValue1(), block.getValue2(), block.getValue3()) instanceof TileEntityRibbonCable)) {
					devices.add(new ObjectTriplet<Integer, Integer, Integer>(nextBlock.getValue1(), nextBlock.getValue2(), nextBlock.getValue3()));
					break;
				} else {
					Collection<ObjectTriplet<Integer, Integer, Integer>> result = getDevicesInChain(nextBlock);
					if (result != null)
						devices.addAll(result);
				}
			} else {
				Collection<ObjectTriplet<Integer, Integer, Integer>> result = getDevicesInChain(nextBlock);
				if (result != null)
					devices.addAll(result);
			}
		}
		probeStatus.put(block, false);
		return devices;
	}
}