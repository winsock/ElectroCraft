package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.utils.Orientations;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class NetworkBlock extends IOPortCapableMinecraft {
	
	protected boolean hasBeenNetworkProbed = false;
	protected Map<Integer, NetworkBlock> connectedDevices = new HashMap<Integer, NetworkBlock>();
	protected ComputerNetwork network;
	protected int controlAddress = 0, dataAddress = 0;

	public abstract boolean canConnectNetwork(NetworkBlock block);
	
	public NetworkBlock() {
		super(mod_ElectroCraft.instance.getComputerHandler());
	}
	
	public void setNetworkProbedStatus(boolean status) {
		hasBeenNetworkProbed = status;
	}
	
	public boolean isConnectedToNetwork(NetworkBlock block) {
		for (NetworkBlock testBlock : connectedDevices.values()) {
			if (testBlock.xCoord == block.xCoord && testBlock.yCoord == block.yCoord && testBlock.zCoord == block.zCoord)
				return true;
		}
		return false;
	}
	
	public void update(NetworkBlock block) {
		super.update(block);
	}
	
	public void updateComputerNetwork() {
		this.network = checkConnectedBlocksForComputerNetworks();
		if (network == null)
			network = new ComputerNetwork();
		computeConnections();
		network.updateProviderChain(this);
	}
	
	public void setControlAddress(int controlAddress) {
		this.controlAddress = controlAddress;
		this.network.registerIOPort(this);
	}
	
	public void setDataAddress(int dataAddress) {
		this.dataAddress = dataAddress;
		this.network.registerIOPort(this);
	}
	
	@Override
	public int[] ioPortsRequested() {
		return new int[] { controlAddress, dataAddress };
	}
	
	public ComputerNetwork checkConnectedBlocksForComputerNetworks() {
		ComputerNetwork network = this.network;
		for (NetworkBlock block : connectedDevices.values()) {
			if (block.network != null) {
				if (network != null) {
					network.mergeNetwork(block.network);
					block.network = network;
					block.needsUpdate = true;
				} else {
					network = block.network;
				}
			}
		}
		return network;
	}
	
	public void computeConnections() {
		connectedDevices.clear();
		// X
		if (worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord) instanceof NetworkBlock) {
			if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord)))
				connectedDevices.put(Orientations.XPos.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord));
		}
		if (worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord) instanceof NetworkBlock) {
			if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord)))
				connectedDevices.put(Orientations.XNeg.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord));
		}
		
		// Y
		if (worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord) instanceof NetworkBlock) {
			if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord)))
				connectedDevices.put(Orientations.YPos.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord));
		}
		if (worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord) instanceof NetworkBlock) {
			if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord)))
				connectedDevices.put(Orientations.YNeg.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord));
		}
		
		// Z
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1) instanceof NetworkBlock) {
			if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1)))
				connectedDevices.put(Orientations.ZPos.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1)); 
		}
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1) instanceof NetworkBlock) {
			if (canConnectNetwork((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1)))
				connectedDevices.put(Orientations.ZNeg.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1));
		}
	}

	public ComputerNetwork getComputerNetwork() {
		return network;
	}
}
