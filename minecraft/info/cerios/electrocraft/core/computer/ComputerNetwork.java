package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ComputerNetwork {
	protected Set<TileEntityComputer> computers;
	
	public void updateProviderChain(NetworkBlock startBlock) {
		computers = new HashSet<TileEntityComputer>(getComputersInChain(startBlock));
	}
	
	public void mergeNetwork(ComputerNetwork network) {
		computers.addAll(network.computers);
	}
	
	public Set<TileEntityComputer> getComputers() {
		return computers;
	}

	private Collection<TileEntityComputer> getComputersInChain(NetworkBlock block) {
		if (block.getProbedStatus())
			return null;
		block.setProbedStatus(true);
		
		// Check if the block is a electricity provider
		Set<TileEntityComputer> providers = new HashSet<TileEntityComputer>();
		if (block instanceof TileEntityComputer)
			providers.add((TileEntityComputer) block);
		
		// Check if any connected blocks are providers
		for (NetworkBlock nextBlock : block.connectedDevices.values()) {
			// Exit recursion when provider found
			if (nextBlock instanceof TileEntityComputer) {
				providers.add((TileEntityComputer) nextBlock);
				for (NetworkBlock providerBlock : nextBlock.connectedDevices.values()) {
					if (providerBlock instanceof TileEntityComputer)
						providers.add((TileEntityComputer) providerBlock);
				}
				break;
			} else {
				Collection<TileEntityComputer> result = getComputersInChain(nextBlock);
				if (result != null)
					providers.addAll(result);
			}
		}
		block.setProbedStatus(false);
		return providers;
	}
}
