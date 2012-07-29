package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.jpc.emulator.PC;

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
	
	public void registerIOPort(IOPortCapableMinecraft ioPort) {
		for (TileEntityComputer computer : computers) {
			mod_ElectroCraft.instance.getComputerHandler().stopComputer(computer.getComputer());
			computer.getComputer().addPart(ioPort);
			computer.getComputer().reset();
		}
	}
	
	public void removeIOPort(IOPortCapableMinecraft ioPort) {
		for (TileEntityComputer computer : computers) {
			mod_ElectroCraft.instance.getComputerHandler().stopComputer(computer.getComputer());
			computer.getComputer().removePart(ioPort);
			computer.getComputer().reset();
		}
	}
	
	public void resetComputers() {
		for (TileEntityComputer computer : computers)
			computer.reset();
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
