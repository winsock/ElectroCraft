package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.AbstractElectroCraftMod;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRibbonCable;
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
	protected Set<ObjectTriplet<Integer, Integer, Integer>> devices = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
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
				
				devices.add(new ObjectTriplet<Integer, Integer, Integer>(x, y, z));
			}
		}
	}
	
	public void updateProviderChain(NetworkBlock startBlock) {
		devices = getDevicesInChain(new ObjectTriplet<Integer, Integer, Integer>(startBlock.xCoord, startBlock.yCoord, startBlock.zCoord));
		probeStatus.clear();
	}
	
	public void mergeNetwork(ComputerNetwork network) {
		devices.addAll(network.devices);
	}
	
	public Set<TileEntityComputer> getComputers() {
		Set<TileEntityComputer> tempSet = new HashSet<TileEntityComputer>();
		for (ObjectTriplet<Integer, Integer, Integer> computer : devices) {
			TileEntityComputer computerTileEntity = getTileEntityComputerFromLocation(computer.getValue1(), computer.getValue2(), computer.getValue3());
			if (computerTileEntity == null)
				continue;
			tempSet.add(computerTileEntity);
		}
		return tempSet;
	}

	public TileEntityComputer getTileEntityComputerFromLocation(int x, int y, int z) {
		if (AbstractElectroCraftMod.getInstance().getSidedMethods().getBlockTileEntity(x, y, z) instanceof TileEntityComputer) {
			return (TileEntityComputer) AbstractElectroCraftMod.getInstance().getSidedMethods().getBlockTileEntity(x, y, z);
		}
		return null;
	}
	
	public NetworkBlock getNetworkBlockFromLocation(int x, int y, int z) {
		if (AbstractElectroCraftMod.getInstance().getSidedMethods().getBlockTileEntity(x, y, z) instanceof NetworkBlock) {
			return (NetworkBlock) AbstractElectroCraftMod.getInstance().getSidedMethods().getBlockTileEntity(x, y, z);
		}
		return null;
	}

	private Set<ObjectTriplet<Integer, Integer, Integer>> getDevicesInChain(ObjectTriplet<Integer, Integer, Integer> block) {
		if (probeStatus.get(block) != null && probeStatus.get(block))
			return new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
		probeStatus.put(block, true);
		
		if (getNetworkBlockFromLocation(block.getValue1(), block.getValue2(), block.getValue3()) == null) {
			return new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
		}
		
		Set<ObjectTriplet<Integer, Integer, Integer>> connections = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
		NetworkBlock netBlock = getNetworkBlockFromLocation(block.getValue1(), block.getValue2(), block.getValue3());
		if (!(netBlock instanceof TileEntityRibbonCable)) {
			connections.add(block);
		}
		for (ObjectTriplet<Integer, Integer, Integer> connection : netBlock.connectedDevices.values()) {
			if (!(getNetworkBlockFromLocation(block.getValue1(), block.getValue2(), block.getValue3()) instanceof TileEntityRibbonCable))
				connections.add(connection);
			connections.addAll(getDevicesInChain(connection));
		}
		
		return connections;
	}
}
