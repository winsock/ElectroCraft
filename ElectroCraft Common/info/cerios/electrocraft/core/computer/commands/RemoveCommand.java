package info.cerios.electrocraft.core.computer.commands;

import java.io.File;

import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.luaapi.ComputerFile;

public class RemoveCommand implements IComputerCommand {
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		ComputerFile file = new ComputerFile(computer.getBaseDirectory() + "/" + computer.getCurrentDirectory() + "/" + argv[0], computer);
		if (file.exists()) {
			file.delete();
			if (file.exists()) {
				computer.getTerminal().print("Unable to remove requested file or folder!");
			}
		} else
			computer.getTerminal().print("ERROR: The requested file or folder does not exist!");
	}
}
