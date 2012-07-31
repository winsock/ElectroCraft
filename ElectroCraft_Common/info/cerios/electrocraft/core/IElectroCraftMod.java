package info.cerios.electrocraft.core;

import info.cerios.electrocraft.core.computer.IComputerHandler;

public interface IElectroCraftMod {
	public IComputerHandler getComputerHandler();
	public boolean isShiftHeld();
	public IMinecraftMethods getSidedMethods();
}
