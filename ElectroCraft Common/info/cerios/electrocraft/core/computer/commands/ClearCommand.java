package info.cerios.electrocraft.core.computer.commands;

import info.cerios.electrocraft.core.computer.Computer;

public class ClearCommand implements IComputerCommand {
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		computer.getTerminal().clear();
	}
}
