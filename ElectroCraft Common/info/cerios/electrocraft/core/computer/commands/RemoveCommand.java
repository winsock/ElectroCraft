package info.cerios.electrocraft.core.computer.commands;

import java.io.File;

import info.cerios.electrocraft.core.computer.Computer;

public class RemoveCommand implements IComputerCommand {
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		File file = new File(computer.getBaseDirectory() + File.separator + computer.getCurrentDirectory() + File.separator + argv[0]);
		if (file.exists()) {
			file.delete();
			if (file.exists()) {
				computer.getTerminal().print("Unable to remove requested file or folder!");
			}
		} else
			computer.getTerminal().print("ERROR: The requested file or folder does not exist!");
	}
}
