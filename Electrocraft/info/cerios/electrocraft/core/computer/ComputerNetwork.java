package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntitySerialCable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.common.util.Constants;

public class ComputerNetwork {
    private Set<BlockPos> devices = new HashSet<BlockPos>();
    private Map<BlockPos, Boolean> probeStatus = new HashMap<BlockPos, Boolean>();
    private Set<Integer> dims = new HashSet<Integer>();

    public ComputerNetwork(Integer... dims) {
        this.dims.addAll(Arrays.asList(dims));
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
        NBTTagList computerList = new NBTTagList();
        if (devices != null) {
            for (BlockPos computer : devices) {
                NBTTagCompound computerData = new NBTTagCompound();
                computerData.setInteger("x", computer.getX());
                computerData.setInteger("y", computer.getY());
                computerData.setInteger("z", computer.getZ());
                computerList.appendTag(computerData);
            }
        }
        nbttagcompound.setTag("devices", computerList);
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        NBTTagList computerList = nbttagcompound.getTagList("devices", Constants.NBT.TAG_LIST);
        for (int i = 0; i < computerList.tagCount(); i++) {
            NBTTagCompound computerData = computerList.getCompoundTagAt(i);
            int x = computerData.getInteger("x");
            int y = computerData.getInteger("y");
            int z = computerData.getInteger("z");

            devices.add(new BlockPos(x, y, z));
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
        devices = getDevicesInChain(startBlock.getPos(), d);
        probeStatus.clear();
    }

    public void mergeNetwork(ComputerNetwork network) {
        devices.addAll(network.devices);
    }

    public Set<TileEntityComputer> getComputers(int d) {
        Set<TileEntityComputer> tempSet = new HashSet<TileEntityComputer>();
        for (BlockPos computer : devices) {
            TileEntityComputer computerTileEntity = getTileEntityComputerFromLocation(computer, d);
            if (computerTileEntity == null) {
                continue;
            }
            tempSet.add(computerTileEntity);
        }
        return tempSet;
    }

    public TileEntityComputer getTileEntityComputerFromLocation(BlockPos blockPos, int d) {
        if (dims.contains(d)) {
            TileEntity tile = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(d).getTileEntity(blockPos);
            if (tile instanceof TileEntityComputer)
                return (TileEntityComputer) tile;
        }
        return null;
    }

    public NetworkBlock getNetworkBlockFromLocation(BlockPos blockPos, int d) {
        if (dims.contains(d)) {
            TileEntity tile = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(d).getTileEntity(blockPos);
            if (tile instanceof NetworkBlock)
                return (NetworkBlock) tile;
        }
        return null;
    }

    private Set<BlockPos> getDevicesInChain(BlockPos blockPos, int d) {
        if (!dims.contains(d))
            return new HashSet<BlockPos>();
        if (probeStatus.get(blockPos) != null && probeStatus.get(blockPos))
            return new HashSet<BlockPos>();
        probeStatus.put(blockPos, true);

        if (getNetworkBlockFromLocation(blockPos, d) == null)
            return new HashSet<BlockPos>();

        Set<BlockPos> connections = new HashSet<BlockPos>();
        NetworkBlock netBlock = getNetworkBlockFromLocation(blockPos, d);
        if (!(netBlock instanceof TileEntitySerialCable)) {
            connections.add(blockPos);
        }
        for (BlockPos connection : netBlock.getConnectedDevices().values()) {
            if (!(getNetworkBlockFromLocation(blockPos, d) instanceof TileEntitySerialCable)) {
                connections.add(connection);
            }
            connections.addAll(getDevicesInChain(connection, d));
        }

        return connections;
    }
}
