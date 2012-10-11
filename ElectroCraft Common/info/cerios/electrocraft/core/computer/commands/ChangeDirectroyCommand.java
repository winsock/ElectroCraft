package info.cerios.electrocraft.core.computer.commands;

import java.io.File;

import info.cerios.electrocraft.core.computer.Computer;

public class ChangeDirectroyCommand implements IComputerCommand {
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		File newDirectory = new File(computer.getBaseDirectory() + File.separator + computer.getCurrentDirectory() + File.separator + argv[0]);
		if (newDirectory.exists())
			computer.setCurrentDirectory(computer.getCurrentDirectory().isEmpty() ? argv[0] : computer.getCurrentDirectory() + File.separator + argv[0]);
		else
			computer.getTerminal().print("ERROR: The requested directory does not exist!");
	}
}
