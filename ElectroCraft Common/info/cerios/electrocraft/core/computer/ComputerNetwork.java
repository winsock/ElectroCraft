package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.api.utils.ObjectTriplet;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import cpw.mods.fml.common.FMLCommonHandler;

public class ComputerNetwork {
	private Set<ObjectTriplet<Integer, Integer, Integer>> devices = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
	private Map<ObjectTriplet<Integer, Integer, Integer>, Boolean> probeStatus = new HashMap<ObjectTriplet<Integer, Integer, Integer>, Boolean>();
	private Set<Integer> dims = new HashSet<Integer>();

	public ComputerNetwork(Integer... dims) {
		this.dims.addAll(Arrays.asList(dims));
	}

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
				NBTTagCompound computerData = (NBTTagCompound) computerList
						.tagAt(i);
				int x = computerData.getInteger("x");
				int y = computerData.getInteger("y");
				int z = computerData.getInteger("z");

				devices.add(new ObjectTriplet<Integer, Integer, Integer>(x, y,
						z));
			}
		}
	}

	public void registerDevice(NetworkBlock device) {
		for (Integer dim : dims) {
			for (TileEntityComputer computer : getComputers(dim)) {
				computer.registerIoPort(device);
			}
		}
	}

	public void removeDevice(NetworkBlock device) {
		for (Integer dim : dims) {
			for (TileEntityComputer computer : getComputers(dim)) {
				computer.removeIoPort(device);
			}
		}
	}

	public void updateProviderChain(NetworkBlock startBlock, int d) {
		devices = getDevicesInChain(
				new ObjectTriplet<Integer, Integer, Integer>(startBlock.xCoord,
						startBlock.yCoord, startBlock.zCoord), d);
		probeStatus.clear();
	}

	public void mergeNetwork(ComputerNetwork network) {
		devices.addAll(network.devices);
	}

	public Set<TileEntityComputer> getComputers(int d) {
		Set<TileEntityComputer> tempSet = new HashSet<TileEntityComputer>();
		for (ObjectTriplet<Integer, Integer, Integer> computer : devices) {
			TileEntityComputer computerTileEntity = getTileEntityComputerFromLocation(
					computer.getValue1(), computer.getValue2(),
					computer.getValue3(), d);
			if (computerTileEntity == null) {
				continue;
			}
			tempSet.add(computerTileEntity);
		}
		return tempSet;
	}

	public TileEntityComputer getTileEntityComputerFromLocation(int x, int y,
			int z, int d) {
		if (dims.contains(d)) {
			TileEntity tile = FMLCommonHandler.instance()
					.getMinecraftServerInstance().worldServerForDimension(d)
					.getBlockTileEntity(x, y, z);
			if (tile instanceof TileEntityComputer)
				return (TileEntityComputer) tile;
		}
		return null;
	}

	public NetworkBlock getNetworkBlockFromLocation(int x, int y, int z, int d) {
		if (dims.contains(d)) {
			TileEntity tile = FMLCommonHandler.instance()
					.getMinecraftServerInstance().worldServerForDimension(d)
					.getBlockTileEntity(x, y, z);
			if (tile instanceof NetworkBlock)
				return (NetworkBlock) tile;
		}
		return null;
	}

	private Set<ObjectTriplet<Integer, Integer, Integer>> getDevicesInChain(
			ObjectTriplet<Integer, Integer, Integer> block, int d) {
		if (!dims.contains(d))
			return new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
		if (probeStatus.get(block) != null && probeStatus.get(block))
			return new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
		probeStatus.put(block, true);

		if (getNetworkBlockFromLocation(block.getValue1(), block.getValue2(),
				block.getValue3(), d) == null)
			return new HashSet<ObjectTriplet<Integer, Integer, Integer>>();

		Set<ObjectTriplet<Integer, Integer, Integer>> connections = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
		NetworkBlock netBlock = getNetworkBlockFromLocation(block.getValue1(),
				block.getValue2(), block.getValue3(), d);
		if (!(netBlock instanceof TileEntitySerialCable)) {
			connections.add(block);
		}
		for (ObjectTriplet<Integer, Integer, Integer> connection : netBlock
				.getConnectedDevices().values()) {
			if (!(getNetworkBlockFromLocation(block.getValue1(),
					block.getValue2(), block.getValue3(), d) instanceof TileEntitySerialCable)) {
				connections.add(connection);
			}
			connections.addAll(getDevicesInChain(connection, d));
		}

		return connections;
	}
}
