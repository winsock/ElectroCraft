package info.cerios.electrocraft.core.computer.commands;

import info.cerios.electrocraft.core.computer.Computer;

public interface IComputerCommand {
	public void onCommand(Computer computer, int argc, String[] argv);
}
