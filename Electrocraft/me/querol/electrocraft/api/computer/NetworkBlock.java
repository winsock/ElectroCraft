package me.querol.electrocraft.api.computer;

import me.querol.electrocraft.core.blocks.tileentities.ElectroTileEntity;
import me.querol.electrocraft.core.computer.Computer;
import me.querol.electrocraft.core.computer.ComputerNetwork;

import java.util.HashMap;
import java.util.Map;

import me.querol.electrocraft.core.blocks.tileentities.ElectroTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public abstract class NetworkBlock extends ElectroTileEntity implements IUpdatePlayerListBox {
    protected boolean hasBeenNetworkProbed = false;
    protected Map<EnumFacing, BlockPos> connectedDevices = new HashMap<EnumFacing, BlockPos>();
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
        for (BlockPos testBlockPos : connectedDevices.values()) {
            if (testBlockPos == block.pos)
                return true;
        }
        return false;
    }

    public Map<EnumFacing, BlockPos> getConnectedDevices() {
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
    public void update() {
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
            network = new ComputerNetwork(worldObj.provider.getDimensionId());
        computeNetworkConnections();
        network.updateProviderChain(this, worldObj.provider.getDimensionId());
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

    public NetworkBlock getNetworkBlockFromLocation(BlockPos pos) {
        if (worldObj.getTileEntity(pos) instanceof NetworkBlock)
            return (NetworkBlock) worldObj.getTileEntity(pos);
        return null;
    }

    public boolean isConnectedInDirection(EnumFacing direction) {
        return connectedDevices.containsKey(direction.ordinal());
    }

    public ComputerNetwork checkConnectedBlocksForComputerNetworks() {
        ComputerNetwork network = this.network;
        for (BlockPos blockPos : connectedDevices.values()) {
            NetworkBlock networkBlock = getNetworkBlockFromLocation(blockPos);
            if (networkBlock == null) {
                connectedDevices.remove(blockPos);
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

        for (EnumFacing direction : EnumFacing.values()) {
            if (worldObj.getTileEntity(getPos().offset(direction)) instanceof NetworkBlock)
                connectedDevices.put(direction, getPos().offset(direction));
        }
    }

    public ComputerNetwork getComputerNetwork() {
        return network;
    }
}
