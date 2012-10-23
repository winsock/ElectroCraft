package info.cerios.electrocraft.api;

import java.util.concurrent.FutureTask;

import info.cerios.electrocraft.api.computer.IComputerCallback;
import info.cerios.electrocraft.api.computer.IComputerRunnable;
import info.cerios.electrocraft.api.computer.IMCRunnable;
import info.cerios.electrocraft.api.drone.tools.IDroneTool;
import info.cerios.electrocraft.api.drone.upgrade.ICard;

public interface IElectroCraft {
	public void registerAddon(Object mod);
	public void regsiterCard(ICard card);
	public void registerDroneTool(IDroneTool tool);
	public void registerRunnable(FutureTask<?> task);
}
