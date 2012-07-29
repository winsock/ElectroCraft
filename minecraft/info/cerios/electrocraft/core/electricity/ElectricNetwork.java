package info.cerios.electrocraft.core.electricity;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.utils.ObjectTriplet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

public class ElectricNetwork {
		
	protected Set<ObjectTriplet<Integer, Integer, Integer>> providers;
	protected Map<ObjectTriplet<Integer, Integer, Integer>, Boolean> probeStatus = new HashMap<ObjectTriplet<Integer, Integer, Integer>, Boolean>();

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		NBTTagList providerList = new NBTTagList("providers");
		if (providers != null) {
			for (ObjectTriplet<Integer, Integer, Integer> provider : providers) {
				NBTTagCompound providerData = new NBTTagCompound("providerData");
				providerData.setInteger("x", provider.getValue1());
				providerData.setInteger("y", provider.getValue2());
				providerData.setInteger("z", provider.getValue3());
				providerList.appendTag(providerData);
			}
		}
		nbttagcompound.setTag("providers", providerList);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		NBTTagList providerList = nbttagcompound.getTagList("providers");
		for (int i = 0; i < providerList.tagCount(); i++) {
			if (providerList.tagAt(i) instanceof NBTTagCompound) {
				NBTTagCompound providerData = (NBTTagCompound) providerList.tagAt(i);
				int x = providerData.getInteger("x");
				int y = providerData.getInteger("y");
				int z = providerData.getInteger("z");
				
				if (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z) instanceof ElectricityProvider) {
					if (providers == null)
						providers = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
					providers.add(new ObjectTriplet<Integer, Integer, Integer>(x, y, z));
				}
			}
		}
	}
	
	public void updateProviderChain(ElectricBlock startBlock) {
		providers = getProvidersInChain(new ObjectTriplet<Integer, Integer, Integer>(startBlock.xCoord, startBlock.yCoord, startBlock.zCoord));
	}
	
	public void mergeNetwork(ElectricNetwork network) {
		providers.addAll(network.providers);
	}
	
	public Set<ElectricityProvider> getProviders() {
		Set<ElectricityProvider> tempSet = new HashSet<ElectricityProvider>();
		if (providers == null)
			return null;
		for (ObjectTriplet<Integer, Integer, Integer> provider : providers) {
			if (getProviderFromLocation(provider.getValue1(), provider.getValue2(), provider.getValue3()) != null) {
				tempSet.add(getProviderFromLocation(provider.getValue1(), provider.getValue2(), provider.getValue3()));
			} else {
				providers.remove(provider);
			}
		}
		return tempSet;
	}
	
	public ElectricityProvider getProviderFromLocation(int x, int y, int z) {
		if (ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z) instanceof ElectricityProvider) {
			return (ElectricityProvider) ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z);
		}
		return null;
	}
	
	public ElectricBlock getElectricBlockFromProvider(ElectricityProvider provider) {
		if (provider instanceof ElectricBlock)
			return (ElectricBlock) provider;
		return null;
	}
	
	private Set<ObjectTriplet<Integer, Integer, Integer>> getProvidersInChain(ObjectTriplet<Integer, Integer, Integer> block) {
		if (probeStatus.get(block) != null && probeStatus.get(block))
			return null;
		probeStatus.put(block, true);
		
		// Check if the block is a electricity provider
		Set<ObjectTriplet<Integer, Integer, Integer>> providers = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
		if (getProviderFromLocation(block.getValue1(), block.getValue2(), block.getValue3()) != null)
			providers.add(block);
		else
			return null;
		
		if (getElectricBlockFromProvider(getProviderFromLocation(block.getValue1(), block.getValue2(), block.getValue3())) == null)
			return null;
		
		// Check if any connected blocks are providers
		for (ObjectTriplet<Integer, Integer, Integer> nextBlock : getElectricBlockFromProvider(getProviderFromLocation(block.getValue1(), block.getValue2(), block.getValue3())).connectedDevices.values()) {
			// Exit recursion when provider found
			if (getProviderFromLocation(nextBlock.getValue1(), nextBlock.getValue2(), nextBlock.getValue3()) != null) {
				providers.add(new ObjectTriplet<Integer, Integer, Integer>(nextBlock.getValue1(), nextBlock.getValue2(), nextBlock.getValue3()));
				break;
			} else {
				Collection<ObjectTriplet<Integer, Integer, Integer>> result = getProvidersInChain(nextBlock);
				if (result != null)
					providers.addAll(result);
			}
		}
		probeStatus.put(block, false);
		return providers;
	}
}
