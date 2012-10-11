package info.cerios.electrocraft.core.computer.commands;

import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.luaapi.ComputerFile;

public class ChangeDirectroyCommand implements IComputerCommand {
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		ComputerFile newDirectory = new ComputerFile(computer.getBaseDirectory() + "/" + computer.getCurrentDirectory() + "/" + argv[0], computer);
		if (newDirectory.exists())
			computer.setCurrentDirectory(computer.getCurrentDirectory().isEmpty() ? argv[0] : computer.getCurrentDirectory() + "/" + argv[0]);
		else
			computer.getTerminal().print("ERROR: The requested directory does not exist!");
	}
}
