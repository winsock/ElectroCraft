package info.cerios.electrocraft.core.electricity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagInt;
import net.minecraft.src.NBTTagIntArray;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.forge.ObjectPair;
import info.cerios.electrocraft.core.blocks.ElectroBlock;
import info.cerios.electrocraft.core.blocks.tileentities.ElectroTileEntity;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Orientations;
import info.cerios.electrocraft.core.utils.Utils;

public abstract class ElectricBlock extends ElectroTileEntity {
	
	protected boolean isChanging = false;
	protected boolean isChainingPowerUpdates = false;
	protected boolean hasPowerChanged = false;
	protected boolean needsUpdate = true;
	private boolean hasBeenProbed = false;
	
	protected int currentVoltage = 0;
	protected float currentCurrent = 0.0f;
	protected ElectricityTypes currentElectricityType = ElectricityTypes.VC;
	
	protected Map<Integer, ElectricBlock> connectedDevices = new HashMap<Integer, ElectricBlock>();
	
	protected ElectricNetwork network;
	
	public abstract boolean canConnect(ElectricBlock block);
		
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("currentVoltage", currentVoltage);
		nbttagcompound.setFloat("currentCurrent", currentCurrent);
		nbttagcompound.setString("currentElectricityType", currentElectricityType.name());
		NBTTagList connectedDevices = new NBTTagList();
		for (int orientation : this.connectedDevices.keySet()) {
			ElectricBlock block = this.connectedDevices.get(orientation);
			if (block == null)
				continue;
			NBTTagCompound device = new NBTTagCompound();
			device.setInteger("orientation", orientation);
			device.setIntArray("location", new int[] { block.xCoord, block.yCoord, block.zCoord });
			connectedDevices.appendTag(device);
		}
		nbttagcompound.setTag("connectedDevices", connectedDevices);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		currentVoltage = nbttagcompound.getInteger("currentVoltage");
		currentCurrent = nbttagcompound.getFloat("currentCurrent");
		currentElectricityType = ElectricityTypes.valueOf(nbttagcompound.getString("currentElectricityType"));
		NBTTagList connectedDevices = nbttagcompound.getTagList("connectedDevices");
		for (int i = 0; i < connectedDevices.tagCount(); i++) {
			NBTTagCompound device = (NBTTagCompound)connectedDevices.tagAt(i);
			int[] location = device.getIntArray("location");
			if (worldObj.getBlockTileEntity(location[0], location[1], location[2]) instanceof ElectricBlock)
				this.connectedDevices.put(device.getInteger("orientation"), (ElectricBlock) worldObj.getBlockTileEntity(location[0], location[1], location[2]));
			else
				// Invalid stored location of a electric block, something changed, call update
				update(this);
		}
	}
	
	public void computePower() {
		if (network == null)
			updateNetwork();
		
		Set<ElectricityProvider> providers = network.getProviders();

		// Check for any voltage mismatches and or current type mismatches and get the current, voltage, and current type values
		int lastVoltage = 0;
		ElectricityTypes lastType = ElectricityTypes.UNKNOWN;
		float newCurrent = 0f;
		for (ElectricityProvider provider : providers) {
			// Voltage
			if (provider.getVoltage() > 0)
			{
				if (lastVoltage <= 0) {
					lastVoltage = provider.getVoltage();
				} else if (lastVoltage != provider.getVoltage()) {
					// TODO Trigger an explosion on voltage mismatch
				}
			}

			// Current Type
			if (provider.getTypeOfCurrent() != ElectricityTypes.UNKNOWN) {
				if (lastType == ElectricityTypes.UNKNOWN)
					lastType = provider.getTypeOfCurrent();
				else if (lastType != provider.getTypeOfCurrent()) {
					// TODO Trigger an explosion on current type mismatch
				}
			}

			// Incrementing the current
			// newCurrent += block.currentCurrent;

			// Check if anything changed, if yes set the flag
			if (currentVoltage != lastVoltage || currentCurrent != newCurrent || currentElectricityType != lastType)
				hasPowerChanged = true;

			currentVoltage = lastVoltage;
			currentCurrent = newCurrent;
			currentElectricityType = lastType;
		}
	}
	
	public boolean isConnectedTo(ElectricBlock block) {
		for (ElectricBlock testBlock : connectedDevices.values()) {
			if (testBlock.xCoord == block.xCoord && testBlock.yCoord == block.yCoord && testBlock.zCoord == block.zCoord)
				return true;
		}
		return false;
		
	}
	
	public void setProbedStatus(boolean status) {
		hasBeenProbed = status;
	}
	
	public boolean getProbedStatus() {
		return hasBeenProbed;
	}
	
	public ElectricNetwork getNetwork() {
		return network;
	}
	
	@Override
	public boolean canUpdate() {
		return true;
	}
	
	@Override
	public void updateEntity() {
		if (needsUpdate)
			update(this);
		if (hasPowerChanged)
			hasPowerChanged = false;
	}
	
	public int getVoltage() {
		return currentVoltage;
	}
	
	public float getCurrent() {
		return currentCurrent;
	}
	
	public ElectricityTypes getElectricityType() {
		return currentElectricityType;
	}
	
	/**
	 * Called when the block needs to be updated
	 * @param block The causing block, can be self
	 */
	public void update(ElectricBlock block) {
		// Check to prevent infinite recursion when firing change chain
		if (isChanging)
			return;
		isChanging = true;
		computePower();
		isChanging = false;
		needsUpdate = false;
	}
	
	public void updateNetwork() {
		this.network = checkConnectedBlocksForNetworks();
		if (network == null)
			network = new ElectricNetwork();
		computeConnections();
		network.updateProviderChain(this);
		needsUpdate = true;
	}
	
	public ElectricNetwork checkConnectedBlocksForNetworks() {
		ElectricNetwork network = this.network;
		for (ElectricBlock block : connectedDevices.values()) {
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
	
	public void computeConnectingBlocksPower() {
		for (ElectricBlock device: connectedDevices.values()) {
			device.computePower();
		}
	}
	
	public void computeConnections() {
		connectedDevices.clear();
		// X
		if (worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord) instanceof ElectricBlock) {
			if (canConnect((ElectricBlock) worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord)))
				connectedDevices.put(Orientations.XPos.ordinal(), (ElectricBlock) worldObj.getBlockTileEntity(xCoord + 1, yCoord, zCoord));
		}
		if (worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord) instanceof ElectricBlock) {
			if (canConnect((ElectricBlock) worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord)))
				connectedDevices.put(Orientations.XNeg.ordinal(), (ElectricBlock) worldObj.getBlockTileEntity(xCoord - 1, yCoord, zCoord));
		}
		
		// Y
		if (worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord) instanceof ElectricBlock) {
			if (canConnect((ElectricBlock) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord)))
				connectedDevices.put(Orientations.YPos.ordinal(), (ElectricBlock) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord));
		}
		if (worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord) instanceof ElectricBlock) {
			if (canConnect((ElectricBlock) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord)))
				connectedDevices.put(Orientations.YNeg.ordinal(), (ElectricBlock) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord));
		}
		
		// Z
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1) instanceof ElectricBlock) {
			if (canConnect((ElectricBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1)))
				connectedDevices.put(Orientations.ZPos.ordinal(), (ElectricBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord + 1)); 
		}
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1) instanceof ElectricBlock) {
			if (canConnect((ElectricBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1)))
				connectedDevices.put(Orientations.ZNeg.ordinal(), (ElectricBlock) worldObj.getBlockTileEntity(xCoord, yCoord, zCoord - 1));
		}
	}
}
