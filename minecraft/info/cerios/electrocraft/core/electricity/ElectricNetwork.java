package info.cerios.electrocraft.core.electricity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ElectricNetwork {
		
	protected Set<ElectricityProvider> providers;
	
	public void updateProviderChain(ElectricBlock startBlock) {
		providers = new HashSet<ElectricityProvider>(getProvidersInChain(startBlock));
	}
	
	public void mergeNetwork(ElectricNetwork network) {
		providers.addAll(network.providers);
	}
	
	public Set<ElectricityProvider> getProviders() {
		return providers;
	}

	private Collection<ElectricityProvider> getProvidersInChain(ElectricBlock block) {
		if (block.getProbedStatus())
			return null;
		block.setProbedStatus(true);
		
		// Check if the block is a electricity provider
		Set<ElectricityProvider> providers = new HashSet<ElectricityProvider>();
		if (block instanceof ElectricityProvider)
			providers.add((ElectricityProvider) block);
		
		// Check if any connected blocks are providers
		for (ElectricBlock nextBlock : block.connectedDevices.values()) {
			// Exit recursion when provider found
			if (nextBlock instanceof ElectricityProvider) {
				providers.add((ElectricityProvider) nextBlock);
				for (ElectricBlock providerBlock : nextBlock.connectedDevices.values()) {
					if (providerBlock instanceof ElectricityProvider)
						providers.add((ElectricityProvider) providerBlock);
				}
				break;
			} else {
				Collection<ElectricityProvider> result = getProvidersInChain(nextBlock);
				if (result != null)
					providers.addAll(result);
			}
		}
		block.setProbedStatus(false);
		return providers;
	}
}
