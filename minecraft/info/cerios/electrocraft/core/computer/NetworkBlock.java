package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.utils.Orientations;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class NetworkBlock extends IOPortCapableMinecraft {
	
	protected boolean hasBeenNetworkProbed = false;
	protected Map<Integer, NetworkBlock> connectedDevices = new HashMap<Integer, NetworkBlock>();

	public abstract boolean canConnectNetwork(NetworkBlock block);
	
	public NetworkBlock(ComputerHandler computerHandler) {
		super(computerHandler);
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
	
	public void computeConnections() {
		connectedDevices.clear();
		// X
		if (worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord) instanceof NetworkBlock) {
			if (canConnect((NetworkBlock) worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord)))
				connectedDevices.put(Orientations.XPos.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord));
		}
		if (worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord) instanceof NetworkBlock) {
			if (canConnect((NetworkBlock) worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord)))
				connectedDevices.put(Orientations.XNeg.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord));
		}
		
		// Y
		if (worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord) instanceof NetworkBlock) {
			if (canConnect((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord)))
				connectedDevices.put(Orientations.YPos.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord));
		}
		if (worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord) instanceof NetworkBlock) {
			if (canConnect((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord)))
				connectedDevices.put(Orientations.YNeg.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord));
		}
		
		// Z
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1) instanceof NetworkBlock) {
			if (canConnect((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1)))
				connectedDevices.put(Orientations.ZPos.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1)); 
		}
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1) instanceof NetworkBlock) {
			if (canConnect((NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1)))
				connectedDevices.put(Orientations.ZNeg.ordinal(), (NetworkBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1));
		}
	}
}
