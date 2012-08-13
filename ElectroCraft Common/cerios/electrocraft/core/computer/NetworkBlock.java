package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.blocks.tileentities.ElectroTileEntity;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Orientations;
import net.minecraft.src.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public abstract class NetworkBlock extends ElectroTileEntity {

    protected boolean hasBeenNetworkProbed = false;
    protected Map<Integer, ObjectTriplet<Integer, Integer, Integer>> connectedDevices = new HashMap<Integer, ObjectTriplet<Integer, Integer, Integer>>();
    protected ComputerNetwork network;
    protected int controlAddress = 0, dataAddress = 0;
    private boolean dirty = true, hasBeenChecked = false;

    public abstract boolean canConnectNetwork(NetworkBlock block);

    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setInteger("controlAddress", controlAddress);
        nbttagcompound.setInteger("dataAddress", dataAddress);
//		NBTTagList connectionList = new NBTTagList("connections");
//		for (Entry<Integer, ObjectTriplet<Integer, Integer, Integer>> entry : connectedDevices.entrySet()) {
//			NBTTagCompound connectionData = new NBTTagCompound("computerData");
//			connectionData.setInteger("x", entry.getValue().getValue1());
//			connectionData.setInteger("y", entry.getValue().getValue2());
//			connectionData.setInteger("z", entry.getValue().getValue3());
//			connectionData.setInteger("orientation", entry.getKey());
//			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
//			try {
//				this.saveState(new DataOutputStream(bytesOut));
//				connectionData.setByteArray("deviceState", bytesOut.toByteArray());
//			} catch (IOException e) {
//				ModLoader.getLogger().severe("ElectroCraft: ERROR! Unable to save device state!");
//			}
//			connectionList.appendTag(connectionData);
//		}
//		nbttagcompound.setTag("connections", connectionList);
        if (network != null) {
            network.writeToNBT(nbttagcompound);
        }
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        controlAddress = nbttagcompound.getInteger("controlAddress");
        dataAddress = nbttagcompound.getInteger("dataAddress");
//		NBTTagList connections = nbttagcompound.getTagList("connections");
//		for (int i = 0; i < connections.tagCount(); i++) {
//			if (connections.tagAt(i) instanceof NBTTagCompound) {
//				NBTTagCompound connectionData = (NBTTagCompound) connections.tagAt(i);
//				int x = connectionData.getInteger("x");
//				int y = connectionData.getInteger("y");
//				int z = connectionData.getInteger("z");
//				int orientation = connectionData.getInteger("orientation");
//				try {
//					this.loadState(new DataInputStream(new ByteArrayInputStream(connectionData.getByteArray("deviceState"))));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				connectedDevices.put(orientation, new ObjectTriplet<Integer, Integer, Integer>(x, y, z));
//			}
//		}
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

    public void update(NetworkBlock block) {
        computeNetworkConnections();
        updateComputerNetwork();
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
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
        this.controlAddress = controlAddress;
    }

    public void setDataAddress(int dataAddress) {
        this.dataAddress = dataAddress;
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
        connectedDevices.clear();
        // X
        if (worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord)))
                connectedDevices.put(Orientations.XPos.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord + 1, yCoord, zCoord));
        }
        if (worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord)))
                connectedDevices.put(Orientations.XNeg.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord - 1, yCoord, zCoord));
        }

        // Y
        if (worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord)))
                connectedDevices.put(Orientations.YPos.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord + 1, zCoord));
        }
        if (worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord)))
                connectedDevices.put(Orientations.YNeg.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord - 1, zCoord));
        }

        // Z
        if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1)))
                connectedDevices.put(Orientations.ZPos.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord, zCoord + 1));
        }
        if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1) instanceof NetworkBlock) {
            if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1)))
                connectedDevices.put(Orientations.ZNeg.ordinal(), new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord, zCoord - 1));
        }
    }

    public ComputerNetwork getComputerNetwork() {
        return network;
    }
}
