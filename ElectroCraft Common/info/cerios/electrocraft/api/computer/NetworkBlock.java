package info.cerios.electrocraft.api.computer;

import info.cerios.electrocraft.api.utils.ObjectTriplet;
import info.cerios.electrocraft.core.blocks.tileentities.ElectroTileEntity;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.ComputerNetwork;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public abstract class NetworkBlock extends ElectroTileEntity {
    protected boolean hasBeenNetworkProbed = false;
    protected Map<Integer, ObjectTriplet<Integer, Integer, Integer>> connectedDevices = new HashMap<Integer, ObjectTriplet<Integer, Integer, Integer>>();
    protected ComputerNetwork network;
    protected int controlAddress = 0, dataAddress = 0;
    private boolean dirty = true, hasBeenChecked = false;

    public abstract boolean canConnectNetwork(NetworkBlock block);
    public abstract void tick(Computer computer);
    
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setInteger("controlAddress", controlAddress);
        nbttagcompound.setInteger("dataAddress", dataAddress);
        if (network != null) {
            network.writeToNBT(nbttagcompound);
        }
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        controlAddress = nbttagcompound.getInteger("controlAddress");
        dataAddress = nbttagcompound.getInteger("dataAddress");
        dirty = true;
    }

    public void setNetworkProbedStatus(boolean status) {
        hasBeenNetworkProbed = status;
    }

    public boolean isConnectedToNetwork(NetworkBlock block) {
        for (ObjectTriplet<Integer, Integer, Integer> testBlock : connectedDevices.values()) {
            if (testBlock.getValue1() == block.xCoord && testBlock.getValue2() == block.yCoord && testBlock.getValue3() == block.zCoord)
                return true;
        }
        return false;
    }
    
    public Map<Integer, ObjectTriplet<Integer, Integer, Integer>> getConnectedDevices() {
    	return connectedDevices;
    }

    public void update(NetworkBlock block) {
        computeNetworkConnections();
        updateComputerNetwork();
        if (network != null) {
        	network.registerDevice(this);
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote) {
            computeNetworkConnections();
        	return;
        }
        if (dirty) {
            update(this);
            dirty = false;
        }
    }

    public void updateComputerNetwork() {
        this.network = checkConnectedBlocksForComputerNetworks();
        if (network == null)
            network = new ComputerNetwork(worldObj.getWorldInfo().getDimension());
        computeNetworkConnections();
        network.updateProviderChain(this, worldObj.getWorldInfo().getDimension());
        this.network = checkConnectedBlocksForComputerNetworks();
    }

    public void setControlAddress(int controlAddress) {
    	if (network != null)
    		network.registerDevice(this);
        this.controlAddress = controlAddress;
        if (network != null)
    		network.removeDevice(this);
    }

    public void setDataAddress(int dataAddress) {
    	if (network != null)
    		network.removeDevice(this);
        this.dataAddress = dataAddress;
        if (network != null)
    		network.registerDevice(this);
    }

    public int getControlAddress() {
        return controlAddress;
    }

    public int getDataAddress() {
        return dataAddress;
    }

    public NetworkBlock getNetworkBlockFromLocation(int x, int y, int z) {
        if (worldObj.getBlockTileEntity(x, y, z) instanceof NetworkBlock)
            return (NetworkBlock) worldObj.getBlockTileEntity(x, y, z);
        return null;
    }
    
    public boolean isConnectedInDirection(ForgeDirection direction) {
    	return connectedDevices.containsKey(direction.ordinal());
    }

    public ComputerNetwork checkConnectedBlocksForComputerNetworks() {
        ComputerNetwork network = this.network;
        for (ObjectTriplet<Integer, Integer, Integer> block : connectedDevices.values()) {
            NetworkBlock networkBlock = getNetworkBlockFromLocation(block.getValue1(), block.getValue2(), block.getValue3());
            if (networkBlock == null) {
                connectedDevices.remove(block);
                continue;
            }
            if (networkBlock.network != null) {
                if (network != null) {
                    if (networkBlock.network == this.network) {
                        continue;
                    }
                    network.mergeNetwork(networkBlock.network);
                    networkBlock.network = network;
                    networkBlock.dirty = true;
                } else {
                    network = networkBlock.network;
                }
            } else {
                hasBeenChecked = true;
                networkBlock.checkConnectedBlocksForComputerNetworks();
                hasBeenChecked = false;

                if (networkBlock.network != null) {
                    if (network != null) {
                        network.mergeNetwork(networkBlock.network);
                        networkBlock.network = network;
                        networkBlock.dirty = true;
                    } else {
                        network = networkBlock.network;
                    }
                }
            }
        }
        return network;
    }

    public void computeNetworkConnections() {
    	if (network != null)
    		network.removeDevice(this);
        connectedDevices.clear();
        // X
        if (worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord)))
                connectedDevices.put(ForgeDirection.EAST.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord + 1, yCoord, zCoord));
        }
        if (worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord)))
                connectedDevices.put(ForgeDirection.WEST.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord - 1, yCoord, zCoord));
        }

        // Y
        if (worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord)))
                connectedDevices.put(ForgeDirection.UP.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord + 1, zCoord));
        }
        if (worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord)))
                connectedDevices.put(ForgeDirection.DOWN.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord - 1, zCoord));
        }

        // Z
        if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1)))
                connectedDevices.put(ForgeDirection.SOUTH.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord, zCoord + 1));
        }
        if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1)))
                connectedDevices.put(ForgeDirection.NORTH.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord, zCoord - 1));
        }
    }

    public ComputerNetwork getComputerNetwork() {
        return network;
    }
}
